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

package io.sunflower.gizmo.websocket.server.support;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.sunflower.gizmo.Context;
import io.sunflower.gizmo.Result;
import io.sunflower.gizmo.Results;
import io.sunflower.gizmo.server.UndertowContext;
import io.sunflower.gizmo.websocket.WebSocketExtension;
import io.sunflower.gizmo.websocket.WebSocketHandler;
import io.sunflower.gizmo.websocket.WebSocketSession;
import io.sunflower.gizmo.websocket.server.HandshakeFailureException;
import io.sunflower.gizmo.websocket.server.RequestUpgradeStrategy;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.HttpUpgradeListener;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSocketLogger;
import io.undertow.websockets.core.WebSocketVersion;
import io.undertow.websockets.core.protocol.Handshake;
import io.undertow.websockets.core.protocol.version07.Hybi07Handshake;
import io.undertow.websockets.core.protocol.version08.Hybi08Handshake;
import io.undertow.websockets.core.protocol.version13.Hybi13Handshake;
import io.undertow.websockets.spi.AsyncWebSocketHttpServerExchange;
import org.xnio.StreamConnection;

/**
 * DefaultRequestUpgradeStrategy
 *
 * @author michael
 * @date 17/10/14 18:38
 */
public class DefaultRequestUpgradeStrategy implements RequestUpgradeStrategy {

  private final Set<WebSocketChannel> peerConnections = Collections
      .newSetFromMap(new ConcurrentHashMap<WebSocketChannel, Boolean>());

  private final Set<Handshake> handshakes;
  private final Set<WebSocketSession> sessions;

  public DefaultRequestUpgradeStrategy(Set<WebSocketSession> sessions) {
    Set<Handshake> handshakes = new HashSet<>();
    handshakes.add(new Hybi13Handshake());
    handshakes.add(new Hybi08Handshake());
    handshakes.add(new Hybi07Handshake());
    this.handshakes = handshakes;
    this.sessions = sessions;
  }

  @Override
  public String[] getSupportedVersions() {
    return new String[]{
        WebSocketVersion.V13.toHttpHeaderValue(),
        WebSocketVersion.V08.toHttpHeaderValue(),
        WebSocketVersion.V07.toHttpHeaderValue()
    };
  }

  @Override
  public List<WebSocketExtension> getSupportedExtensions(Context context) {
    return Collections.emptyList();
  }

  @Override
  public Result upgrade(Context context, String selectedProtocol,
      List<WebSocketExtension> selectedExtensions, WebSocketHandler wsHandler,
      Map<String, Object> attributes) throws HandshakeFailureException {

    UndertowContext undertowContext = (UndertowContext) context;

    HttpServerExchange exchange = undertowContext.getExchange();

    final AsyncWebSocketHttpServerExchange facade = new AsyncWebSocketHttpServerExchange(exchange, peerConnections);

    Handshake handshaker = null;
    for (Handshake method : handshakes) {
      if (method.matches(facade)) {
        handshaker = method;
        break;
      }
    }

    if (handshaker == null) {
      return Results.status(Result.SC_404_NOT_FOUND).render(Result.NO_HTTP_BODY);
    } else {
      WebSocketLogger.REQUEST_LOGGER
          .debugf("Attempting websocket handshake with %s on %s", handshaker, exchange);

      final Handshake selected = handshaker;

      exchange.upgradeChannel(new HttpUpgradeListener() {
        @Override
        public void handleUpgrade(StreamConnection streamConnection, HttpServerExchange exchange) {
          WebSocketChannel channel = selected
              .createChannel(facade, streamConnection, facade.getBufferPool());

          peerConnections.add(channel);

          WebSocketSession session = new DefaultWebSocketSession(
              attributes, selectedProtocol, selectedExtensions, channel, context);

          sessions.add(session);

          wsHandler.afterConnectionEstablished(session);
          channel.getReceiveSetter().set(new DefaultWebSocketReceiveListener(session, wsHandler));
          channel.resumeReceives();
        }
      });

      handshaker.handshake(facade);
    }

    return null;
  }

  public Set<WebSocketChannel> getPeerConnections() {
    return peerConnections;
  }
}
