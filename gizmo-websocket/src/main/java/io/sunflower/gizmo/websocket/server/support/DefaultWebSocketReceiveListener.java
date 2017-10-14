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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.sunflower.gizmo.exceptions.GizmoException;
import io.sunflower.gizmo.websocket.BinaryMessage;
import io.sunflower.gizmo.websocket.CloseStatus;
import io.sunflower.gizmo.websocket.PingMessage;
import io.sunflower.gizmo.websocket.PongMessage;
import io.sunflower.gizmo.websocket.TextMessage;
import io.sunflower.gizmo.websocket.WebSocketHandler;
import io.sunflower.gizmo.websocket.WebSocketSession;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.CloseMessage;
import io.undertow.websockets.core.WebSocketChannel;

/**
 * DefaultWebSocketReceiveListener
 *
 * @author michael
 * @date 17/10/14 20:21
 */
public class DefaultWebSocketReceiveListener extends AbstractReceiveListener {

  private final WebSocketSession session;
  private final WebSocketHandler wsHandler;

  public DefaultWebSocketReceiveListener(WebSocketSession session,
      WebSocketHandler wsHandler) {
    this.session = session;
    this.wsHandler = wsHandler;
  }

  @Override
  protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message)
      throws IOException {
    wsHandler.handleMessage(session, new TextMessage(message.getData()));
  }

  @Override
  protected void onFullBinaryMessage(WebSocketChannel channel,
      BufferedBinaryMessage message)
      throws IOException {

    ByteBuffer[] byteBuffers = message.getData().getResource();

    wsHandler.handleMessage(session, new BinaryMessage(merge(byteBuffers)));

    super.onFullBinaryMessage(channel, message);
  }

  @Override
  protected void onCloseMessage(CloseMessage cm, WebSocketChannel channel) {
    wsHandler.afterConnectionClosed(session,
        new CloseStatus(channel.getCloseCode(), channel.getCloseReason()));
  }

  @Override
  protected void onFullPingMessage(WebSocketChannel channel,
      BufferedBinaryMessage message)
      throws IOException {
    super.onFullPingMessage(channel, message);
    wsHandler.handleMessage(session, new PingMessage(merge(message.getData().getResource())));
  }

  @Override
  protected void onFullPongMessage(WebSocketChannel channel,
      BufferedBinaryMessage message)
      throws IOException {
    wsHandler.handleMessage(session, new PongMessage(merge(message.getData().getResource())));
    super.onFullPongMessage(channel, message);
  }

  @Override
  protected void onError(WebSocketChannel channel, Throwable error) {
    wsHandler.handleTransportError(session, error);
    super.onError(channel, error);
  }

  private ByteBuffer merge(ByteBuffer[] byteBuffers) {

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {
      for (ByteBuffer byteBuffer : byteBuffers) {
        byte[] buf = new byte[byteBuffer.remaining()];
        byteBuffer.get(buf);
        out.write(buf);
      }
    } catch (IOException e) {
      throw new GizmoException(500, "error read bytebuffer from websocket channel.");
    }

    return ByteBuffer.wrap(out.toByteArray());
  }
}
