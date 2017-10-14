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

import io.sunflower.gizmo.websocket.CloseStatus;
import io.sunflower.gizmo.websocket.TextMessage;
import io.sunflower.gizmo.websocket.WebSocketHandler;
import io.sunflower.gizmo.websocket.WebSocketSession;


/**
 * A convenient base class for {@link WebSocketHandler} implementations that process binary messages
 * only.
 *
 * <p>Text messages are rejected with {@link CloseStatus#NOT_ACCEPTABLE}. All other methods have
 * empty implementations.
 *
 * @author Rossen Stoyanchev
 * @author Phillip Webb
 * @since 4.0
 */
public class BinaryWebSocketHandler extends AbstractWebSocketHandler {

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    try {
      session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Text messages not supported"));
    } catch (IOException ex) {
      // ignore
    }
  }

}
