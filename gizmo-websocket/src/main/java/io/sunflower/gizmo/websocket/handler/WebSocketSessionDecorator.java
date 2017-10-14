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

package io.sunflower.gizmo.websocket.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ListMultimap;
import io.sunflower.gizmo.websocket.CloseStatus;
import io.sunflower.gizmo.websocket.WebSocketExtension;
import io.sunflower.gizmo.websocket.WebSocketMessage;
import io.sunflower.gizmo.websocket.WebSocketSession;

/**
 * Wraps another {@link WebSocketSession} instance and delegates to it.
 *
 * <p>Also provides a {@link #getDelegate()} method to return the decorated session as well as a
 * {@link #getLastSession()} method to go through all nested delegates and return the "last"
 * session.
 *
 * @author Rossen Stoyanchev
 * @since 4.0.3
 */
public class WebSocketSessionDecorator implements WebSocketSession {

  private final WebSocketSession delegate;

  public WebSocketSessionDecorator(WebSocketSession session) {
    Preconditions.checkNotNull(session, "Delegate WebSocketSessionSession is required");
    this.delegate = session;
  }


  public WebSocketSession getDelegate() {
    return this.delegate;
  }

  public WebSocketSession getLastSession() {
    WebSocketSession result = this.delegate;
    while (result instanceof WebSocketSessionDecorator) {
      result = ((WebSocketSessionDecorator) result).getDelegate();
    }
    return result;
  }

  public static WebSocketSession unwrap(WebSocketSession session) {
    if (session instanceof WebSocketSessionDecorator) {
      return ((WebSocketSessionDecorator) session).getLastSession();
    } else {
      return session;
    }
  }

  @Override
  public String getId() {
    return this.delegate.getId();
  }

  @Override
  public String getUri() {
    return this.delegate.getUri();
  }

  @Override
  public ListMultimap<String, String> getHandshakeHeaders() {
    return delegate.getHandshakeHeaders();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return this.delegate.getAttributes();
  }

  @Override
  public Principal getPrincipal() {
    return this.delegate.getPrincipal();
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return this.delegate.getLocalAddress();
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return this.delegate.getRemoteAddress();
  }

  @Override
  public String getAcceptedProtocol() {
    return this.delegate.getAcceptedProtocol();
  }

  @Override
  public void setTextMessageSizeLimit(int messageSizeLimit) {
    this.delegate.setTextMessageSizeLimit(messageSizeLimit);
  }

  @Override
  public int getTextMessageSizeLimit() {
    return this.delegate.getTextMessageSizeLimit();
  }

  @Override
  public void setBinaryMessageSizeLimit(int messageSizeLimit) {
    this.delegate.setBinaryMessageSizeLimit(messageSizeLimit);
  }

  @Override
  public int getBinaryMessageSizeLimit() {
    return this.delegate.getBinaryMessageSizeLimit();
  }

  @Override
  public boolean isOpen() {
    return this.delegate.isOpen();
  }

  @Override
  public void sendMessage(WebSocketMessage<?> message) throws IOException {
    this.delegate.sendMessage(message);
  }

  @Override
  public List<WebSocketExtension> getServices() {
    return this.delegate.getServices();
  }

  @Override
  public void close() throws IOException {
    this.delegate.close();
  }

  @Override
  public void close(CloseStatus status) throws IOException {
    this.delegate.close(status);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [delegate=" + this.delegate + "]";
  }

}
