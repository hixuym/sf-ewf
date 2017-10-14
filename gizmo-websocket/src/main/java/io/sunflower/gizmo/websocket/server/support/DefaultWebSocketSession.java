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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import io.sunflower.gizmo.Context;
import io.sunflower.gizmo.websocket.BinaryMessage;
import io.sunflower.gizmo.websocket.CloseStatus;
import io.sunflower.gizmo.websocket.PingMessage;
import io.sunflower.gizmo.websocket.PongMessage;
import io.sunflower.gizmo.websocket.TextMessage;
import io.sunflower.gizmo.websocket.WebSocketExtension;
import io.sunflower.gizmo.websocket.handler.AbstractWebSocketSession;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSocketFrameType;

/**
 * DefaultWebSocketSession
 *
 * @author michael
 * @date 17/10/14 19:06
 */
public class DefaultWebSocketSession extends AbstractWebSocketSession {

  public DefaultWebSocketSession(Map<String, Object> attributes,
      String acceptProtocol,
      List<WebSocketExtension> extensions,
      WebSocketChannel channel, Context context) {
    super(attributes);
    this.channel = channel;
    this.context = context;
    this.acceptedProtocol = acceptProtocol;
    this.extensions = extensions;
  }

  private Context context;
  private WebSocketChannel channel;

  @Override
  protected void init() {
    this.id = channel.toString();

    this.headers = ArrayListMultimap.create();

    for (Map.Entry<String, List<String>> e : context.getHeaders().entrySet()) {
      headers.putAll(e.getKey(), e.getValue());
    }

    this.uri = channel.getUrl();
  }

  @Override
  protected void sendTextMessage(TextMessage message) throws IOException {
    channel.send(WebSocketFrameType.TEXT).write(ByteBuffer.wrap(message.asBytes()));
  }

  @Override
  protected void sendBinaryMessage(BinaryMessage message) throws IOException {
    channel.send(WebSocketFrameType.BINARY).write(message.getPayload());
  }

  @Override
  protected void sendPingMessage(PingMessage message) throws IOException {
    channel.send(WebSocketFrameType.PING).write(message.getPayload());
  }

  @Override
  protected void sendPongMessage(PongMessage message) throws IOException {
    channel.send(WebSocketFrameType.PONG).write(message.getPayload());
  }

  @Override
  protected void closeInternal(CloseStatus status) throws IOException {
    CloseMessage closeMessage = new CloseMessage(status.getCode(), status.getReason());
    channel.send(WebSocketFrameType.CLOSE).writeFinal(closeMessage.toByteBuffer());
    channel.close();
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return channel.getLocalAddress(InetSocketAddress.class);
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return channel.getPeerAddress(InetSocketAddress.class);
  }

  @Override
  public boolean isOpen() {
    return channel.isOpen();
  }
}
