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

import com.google.common.base.Preconditions;
import io.sunflower.ewf.websocket.WebSocketMessage;
import io.sunflower.ewf.websocket.CloseStatus;
import io.sunflower.ewf.websocket.WebSocketHandler;
import io.sunflower.ewf.websocket.WebSocketSession;


/**
 * Wraps another {@link WebSocketHandler} instance and delegates to it.
 *
 * <p>Also provides a {@link #getDelegate()} method to return the decorated handler as well as a
 * {@link #getLastHandler()} method to go through all nested delegates and return the "last"
 * handler.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class WebSocketHandlerDecorator implements WebSocketHandler {

  private final WebSocketHandler delegate;

  public WebSocketHandlerDecorator(WebSocketHandler delegate) {
    Preconditions.checkNotNull(delegate, "Delegate must not be null");
    this.delegate = delegate;
  }


  public WebSocketHandler getDelegate() {
    return this.delegate;
  }

  public WebSocketHandler getLastHandler() {
    WebSocketHandler result = this.delegate;
    while (result instanceof WebSocketHandlerDecorator) {
      result = ((WebSocketHandlerDecorator) result).getDelegate();
    }
    return result;
  }

  public static WebSocketHandler unwrap(WebSocketHandler handler) {
    if (handler instanceof WebSocketHandlerDecorator) {
      return ((WebSocketHandlerDecorator) handler).getLastHandler();
    } else {
      return handler;
    }
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    this.delegate.afterConnectionEstablished(session);
  }

  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
      throws IOException {
    this.delegate.handleMessage(session, message);
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    this.delegate.handleTransportError(session, exception);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
    this.delegate.afterConnectionClosed(session, closeStatus);
  }

  @Override
  public boolean supportsPartialMessages() {
    return this.delegate.supportsPartialMessages();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [delegate=" + this.delegate + "]";
  }

}
