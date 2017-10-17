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

import io.sunflower.ewf.websocket.WebSocketMessage;
import io.sunflower.ewf.websocket.CloseStatus;
import io.sunflower.ewf.websocket.WebSocketHandler;
import io.sunflower.ewf.websocket.WebSocketSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A {@link WebSocketHandlerDecorator} that adds logging to WebSocket lifecycle events.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class LoggingWebSocketHandlerDecorator extends WebSocketHandlerDecorator {

  private static final Log logger = LogFactory.getLog(LoggingWebSocketHandlerDecorator.class);


  public LoggingWebSocketHandlerDecorator(WebSocketHandler delegate) {
    super(delegate);
  }


  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    if (logger.isDebugEnabled()) {
      logger.debug("New " + session);
    }
    super.afterConnectionEstablished(session);
  }

  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
    if (logger.isTraceEnabled()) {
      logger.trace("Handling " + message + " in " + session);
    }
    super.handleMessage(session, message);
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    if (logger.isDebugEnabled()) {
      logger.debug("Transport error in " + session, exception);
    }
    super.handleTransportError(session, exception);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
    if (logger.isDebugEnabled()) {
      logger.debug(session + " closed with " + closeStatus);
    }
    super.afterConnectionClosed(session, closeStatus);
  }

}
