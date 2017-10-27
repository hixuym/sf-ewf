/*
 * Copyright (C) 2017. the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sunflower.ewf.websocket.server.support;

import static io.sunflower.ewf.Result.SC_403_FORBIDDEN;
import static io.sunflower.ewf.Result.SC_405_METHOD_NOT_ALLOWED;
import static io.sunflower.ewf.Result.SC_426_UPGRADE_REQUIRED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import io.sunflower.ewf.websocket.SubProtocolCapable;
import io.sunflower.ewf.websocket.WebSocketExtension;
import io.sunflower.ewf.websocket.WebSocketHttpHeaders;
import io.sunflower.ewf.websocket.handler.WebSocketHandlerDecorator;
import io.sunflower.ewf.websocket.server.HandshakeFailureException;
import io.sunflower.ewf.websocket.server.HandshakeHandler;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.Results;
import io.sunflower.ewf.support.Constants;
import io.sunflower.ewf.websocket.WebSocketHandler;
import io.sunflower.ewf.websocket.server.RequestUpgradeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by michael on 16/12/7.
 */
public class DefaultHandshakeHandler implements HandshakeHandler {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final List<String> supportedProtocols = Lists.newArrayList();

  private final RequestUpgradeStrategy requestUpgradeStrategy;

  public DefaultHandshakeHandler(RequestUpgradeStrategy requestUpgradeStrategy) {
    this.requestUpgradeStrategy = requestUpgradeStrategy;
    Preconditions.checkNotNull(requestUpgradeStrategy, "RequestUpgradeStrategy must not be null");
  }

  @Override
  public Result doHandshake(Context context, WebSocketHandler wsHandler,
      Map<String, Object> attributes) throws HandshakeFailureException {

    if (logger.isTraceEnabled()) {
      logger.trace("Processing request " + context.getRoute().getUri() + " with headers=" + context
          .getHeaders());
    }

    try {
      if (!"GET".equalsIgnoreCase(context.getMethod())) {
        if (logger.isErrorEnabled()) {
          logger.error("Handshake failed due to unexpected HTTP method: " + context.getMethod());
        }

        return Results.status(SC_405_METHOD_NOT_ALLOWED).addHeader(HttpHeaders.ALLOW, "GET")
            .render(Result.NO_HTTP_BODY);
      }

      if (!"WebSocket".equalsIgnoreCase(context.getHeader(HttpHeaders.UPGRADE))) {
        if (logger.isErrorEnabled()) {
          logger.error("Handshake failed due to invalid Upgrade header: " + context
              .getHeader(HttpHeaders.UPGRADE));
        }
        return Results.badRequest()
            .renderRaw("Can \"Upgrade\" only to \"WebSocket\".".getBytes(Constants.UTF_8));
      }

      if (!context.getHeader(HttpHeaders.CONNECTION).contains("Upgrade") && !context
          .getHeader(HttpHeaders.CONNECTION).contains("upgrade")) {
        if (logger.isErrorEnabled()) {
          logger.error("Handshake failed due to invalid Connection header " + context
              .getHeader(HttpHeaders.CONNECTION));
        }
        return Results.badRequest()
            .renderRaw("\"Connection\" must be \"upgrade\".".getBytes(Constants.UTF_8));
      }

      if (!isWebSocketVersionSupported(context)) {
        if (logger.isErrorEnabled()) {
          String version = context.getHeader("Sec-WebSocket-Version");
          logger.error("Handshake failed due to unsupported WebSocket version: " + version +
              ". Supported versions: " + Arrays.toString(getSupportedVersions()));
        }
        return Results.status(SC_426_UPGRADE_REQUIRED)
            .addHeader(WebSocketHttpHeaders.SEC_WEBSOCKET_VERSION,
                Joiner.on(",").join(getSupportedVersions()))
            .render(Result.NO_HTTP_BODY);
      }

      if (!isValidOrigin(context)) {
        return Results.status(SC_403_FORBIDDEN).render(Result.NO_HTTP_BODY);
      }

      String wsKey = context.getHeader(WebSocketHttpHeaders.SEC_WEBSOCKET_KEY);
      if (wsKey == null) {
        if (logger.isErrorEnabled()) {
          logger.error("Missing \"Sec-WebSocket-Key\" header");
        }
        return Results.badRequest().render(Result.NO_HTTP_BODY);
      }
    } catch (IOException e) {
      throw new HandshakeFailureException(
          "Response update failed during upgrade to WebSocket: " + context.getRoute().getUri(), e);
    }

    String subProtocol = selectProtocol(
        context.getHeaders().get(WebSocketHttpHeaders.SEC_WEBSOCKET_PROTOCOL), wsHandler);

    List<WebSocketExtension> requested = getSecWebSocketExtensions(
        context.getHeaders().get(WebSocketHttpHeaders.SEC_WEBSOCKET_EXTENSIONS));

    List<WebSocketExtension> supported = this.requestUpgradeStrategy.getSupportedExtensions(context);

    List<WebSocketExtension> extensions = filterRequestedExtensions(context, requested, supported);

    if (logger.isTraceEnabled()) {
      logger.trace(
          "Upgrading to WebSocket, subProtocol=" + subProtocol + ", extensions=" + extensions);
    }

    return this.requestUpgradeStrategy
        .upgrade(context, subProtocol, extensions, wsHandler, attributes);

  }


  /**
   * Returns the value of the {@code Sec-WebSocket-Extensions} header.
   *
   * @return the value of the header
   */
  private List<WebSocketExtension> getSecWebSocketExtensions(List<String> extensions) {
    List<String> values = extensions;
    if (values == null || values.isEmpty()) {
      return Collections.emptyList();
    } else {
      List<WebSocketExtension> result = new ArrayList<>(values.size());
      for (String value : values) {
        result.addAll(WebSocketExtension.parseExtensions(value));
      }
      return result;
    }
  }


  protected boolean isWebSocketVersionSupported(Context context) {
    String version = context.getHeader(WebSocketHttpHeaders.SEC_WEBSOCKET_VERSION);
    String[] supportedVersions = getSupportedVersions();
    for (String supportedVersion : supportedVersions) {
      if (supportedVersion.trim().equals(version)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Perform the sub-protocol negotiation based on requested and supported sub-protocols. For the
   * list of supported sub-protocols, this method first checks if the target WebSocketHandler is and
   * then also checks if any sub-protocols have been explicitly configured with {@link
   * #setSupportedProtocols(String...)}.
   *
   * @param requestedProtocols the requested sub-protocols
   * @param webSocketHandler the WebSocketHandler that will be used
   * @return the selected protocols or {@code null}
   * @see #determineHandlerSupportedProtocols(WebSocketHandler)
   */
  protected String selectProtocol(List<String> requestedProtocols,
      WebSocketHandler webSocketHandler) {
    if (requestedProtocols != null) {
      List<String> handlerProtocols = determineHandlerSupportedProtocols(webSocketHandler);
      for (String protocol : requestedProtocols) {
        if (handlerProtocols.contains(protocol.toLowerCase())) {
          return protocol;
        }
        if (this.supportedProtocols.contains(protocol.toLowerCase())) {
          return protocol;
        }
      }
    }
    return null;
  }

  /**
   * Filter the list of requested WebSocket extensions. <p>As of 4.1, the default implementation of
   * this method filters the list to leave only extensions that are both requested and supported.
   *
   * @param context the current request context
   * @param requestedExtensions the list of extensions requested by the client
   * @param supportedExtensions the list of extensions supported by the server
   * @return the selected extensions or an empty list
   */
  protected List<WebSocketExtension> filterRequestedExtensions(Context context,
      List<WebSocketExtension> requestedExtensions, List<WebSocketExtension> supportedExtensions) {

    List<WebSocketExtension> result = new ArrayList<>(requestedExtensions.size());
    for (WebSocketExtension extension : requestedExtensions) {
      if (supportedExtensions.contains(extension)) {
        result.add(extension);
      }
    }
    return result;
  }

  /**
   * Determine the sub-protocols supported by the given WebSocketHandler by checking whether it is
   * an instance of {@link SubProtocolCapable}.
   *
   * @param handler the handler to check
   * @return a list of supported protocols, or an empty list if none available
   */
  protected final List<String> determineHandlerSupportedProtocols(WebSocketHandler handler) {
    WebSocketHandler handlerToCheck = WebSocketHandlerDecorator.unwrap(handler);
    List<String> subProtocols = null;
    if (handlerToCheck instanceof SubProtocolCapable) {
      subProtocols = ((SubProtocolCapable) handlerToCheck).getSubProtocols();
    }
    return (subProtocols != null ? subProtocols : Collections.emptyList());
  }

  /**
   * Return whether the request {@code Origin} header value is valid or not. By default, all origins
   * as considered as valid. Consider using an OriginHandshakeInterceptor for filtering origins if
   * needed.
   */
  protected boolean isValidOrigin(Context context) {
    return true;
  }

  protected String[] getSupportedVersions() {
    return this.requestUpgradeStrategy.getSupportedVersions();
  }

  public RequestUpgradeStrategy getRequestUpgradeStrategy() {
    return requestUpgradeStrategy;
  }

  /**
   * Use this property to configure the list of supported sub-protocols. The first configured
   * sub-protocol that matches a client-requested sub-protocol is accepted. If there are no matches
   * the response will not contain a {@literal Sec-WebSocket-Protocol} header. <p>Note that if the
   * WebSocketHandler passed in at runtime is an instance of {@link SubProtocolCapable} then there
   * is not need to explicitly configure this property. That is certainly the case with the built-in
   * STOMP over WebSocket support. Therefore this property should be configured explicitly only if
   * the WebSocketHandler does not implement {@code SubProtocolCapable}.
   */
  public void setSupportedProtocols(String... protocols) {
    this.supportedProtocols.clear();
    for (String protocol : protocols) {
      this.supportedProtocols.add(protocol.toLowerCase());
    }
  }

  /**
   * Return the list of supported sub-protocols.
   */
  public String[] getSupportedProtocols() {
    return this.supportedProtocols.toArray(new String[this.supportedProtocols.size()]);
  }

}
