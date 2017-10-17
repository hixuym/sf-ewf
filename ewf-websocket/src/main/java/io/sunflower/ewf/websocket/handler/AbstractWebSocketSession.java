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

package io.sunflower.ewf.websocket.handler;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import io.sunflower.ewf.websocket.BinaryMessage;
import io.sunflower.ewf.websocket.PingMessage;
import io.sunflower.ewf.websocket.PongMessage;
import io.sunflower.ewf.websocket.TextMessage;
import io.sunflower.ewf.websocket.WebSocketExtension;
import io.sunflower.ewf.websocket.WebSocketMessage;
import io.sunflower.ewf.websocket.WebSocketSession;
import io.sunflower.ewf.websocket.CloseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by michael on 16/12/7.
 */
public abstract class AbstractWebSocketSession implements WebSocketSession {

  protected static final Logger logger = LoggerFactory.getLogger(WebSocketSession.class);


  private final Map<String, Object> attributes = Maps.newConcurrentMap();

  protected String acceptedProtocol;
  protected List<WebSocketExtension> extensions;
  protected String id;
  protected String uri;
  protected Principal user;
  protected ListMultimap<String, String> headers;

  private int textMessageSizeLimit;
  private int binaryMessageSizeLimit;

  /**
   * Create a new instance and associate the given attributes with it.
   *
   * @param attributes attributes from the HTTP handshake to associate with the WebSocket session;
   * the provided attributes are copied, the original map is not used.
   */
  public AbstractWebSocketSession(Map<String, Object> attributes) {
    if (attributes != null) {
      this.attributes.putAll(attributes);
    }
  }

  public AbstractWebSocketSession(Map<String, Object> attributes, Principal user) {
    if (attributes != null) {
      this.attributes.putAll(attributes);
    }
    this.user = user;
  }

  protected abstract void init();

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getUri() {
    return this.uri;
  }

  @Override
  public Principal getPrincipal() {
    return this.user;
  }

  @Override
  public List<WebSocketExtension> getServices() {
    return this.extensions;
  }

  @Override
  public ListMultimap<String, String> getHandshakeHeaders() {
    return this.headers;
  }

  @Override
  public String getAcceptedProtocol() {
    return this.acceptedProtocol;
  }

  @Override
  public void setTextMessageSizeLimit(int messageSizeLimit) {
    this.textMessageSizeLimit = messageSizeLimit;
  }

  @Override
  public int getTextMessageSizeLimit() {
    return this.textMessageSizeLimit;
  }

  @Override
  public void setBinaryMessageSizeLimit(int messageSizeLimit) {
    this.binaryMessageSizeLimit = messageSizeLimit;
  }

  @Override
  public int getBinaryMessageSizeLimit() {
    return this.binaryMessageSizeLimit;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return this.attributes;
  }

  @Override
  public final void sendMessage(WebSocketMessage<?> message) throws IOException {

    if (logger.isTraceEnabled()) {
      logger.trace("Sending " + message + ", " + this);
    }

    if (message instanceof TextMessage) {
      sendTextMessage((TextMessage) message);
    } else if (message instanceof BinaryMessage) {
      sendBinaryMessage((BinaryMessage) message);
    } else if (message instanceof PingMessage) {
      sendPingMessage((PingMessage) message);
    } else if (message instanceof PongMessage) {
      sendPongMessage((PongMessage) message);
    } else {
      throw new IllegalStateException("Unexpected WebSocketMessage type: " + message);
    }
  }

  protected abstract void sendTextMessage(TextMessage message) throws IOException;

  protected abstract void sendBinaryMessage(BinaryMessage message) throws IOException;

  protected abstract void sendPingMessage(PingMessage message) throws IOException;

  protected abstract void sendPongMessage(PongMessage message) throws IOException;

  @Override
  public final void close() throws IOException {
    close(CloseStatus.NORMAL);
  }

  @Override
  public final void close(CloseStatus status) throws IOException {
    if (logger.isDebugEnabled()) {
      logger.debug("Closing " + this);
    }
    closeInternal(status);
  }

  protected abstract void closeInternal(CloseStatus status) throws IOException;

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[id=" + getId() + ", uri=" + getUri() + "]";
  }

}
