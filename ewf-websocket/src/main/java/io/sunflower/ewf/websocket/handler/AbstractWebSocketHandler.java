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

import io.sunflower.ewf.websocket.BinaryMessage;
import io.sunflower.ewf.websocket.PongMessage;
import io.sunflower.ewf.websocket.TextMessage;
import io.sunflower.ewf.websocket.WebSocketMessage;
import io.sunflower.ewf.websocket.WebSocketSession;
import io.sunflower.ewf.websocket.CloseStatus;
import io.sunflower.ewf.websocket.WebSocketHandler;

/**
 * A convenient base class for {@link WebSocketHandler} implementation with empty methods.
 *
 * @author Rossen Stoyanchev
 * @author Phillip Webb
 * @since 4.0
 */
public abstract class AbstractWebSocketHandler implements WebSocketHandler {

  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
    if (message instanceof TextMessage) {
      handleTextMessage(session, (TextMessage) message);
    } else if (message instanceof BinaryMessage) {
      handleBinaryMessage(session, (BinaryMessage) message);
    } else if (message instanceof PongMessage) {
      handlePongMessage(session, (PongMessage) message);
    } else {
      throw new IllegalStateException("Unexpected WebSocket message type: " + message);
    }
  }

  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
  }

  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws IOException {
  }

  protected void handlePongMessage(WebSocketSession session, PongMessage message) throws IOException {
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }


}
