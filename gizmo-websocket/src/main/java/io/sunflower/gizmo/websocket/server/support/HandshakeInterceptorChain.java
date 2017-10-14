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
import java.util.List;
import java.util.Map;

import io.sunflower.gizmo.Context;
import io.sunflower.gizmo.websocket.WebSocketHandler;
import io.sunflower.gizmo.websocket.server.HandshakeInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that assists with invoking a list of handshake interceptors.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class HandshakeInterceptorChain {

  private static final Logger logger = LoggerFactory.getLogger(HandshakeInterceptorChain.class);

  private final List<HandshakeInterceptor> interceptors;

  private final WebSocketHandler wsHandler;

  private int interceptorIndex = -1;


  public HandshakeInterceptorChain(List<HandshakeInterceptor> interceptors,
      WebSocketHandler wsHandler) {
    this.interceptors = (interceptors != null ? interceptors : Collections.emptyList());
    this.wsHandler = wsHandler;
  }


  public boolean applyBeforeHandshake(Context context, Map<String, Object> attributes) throws Exception {

    for (int i = 0; i < this.interceptors.size(); i++) {
      HandshakeInterceptor interceptor = this.interceptors.get(i);
      if (!interceptor.beforeHandshake(context, this.wsHandler, attributes)) {
        if (logger.isDebugEnabled()) {
          logger.debug(interceptor + " returns false from beforeHandshake - precluding handshake");
        }
        applyAfterHandshake(context, null);
        return false;
      }
      this.interceptorIndex = i;
    }
    return true;
  }

  public void applyAfterHandshake(Context context, Exception failure) {
    for (int i = this.interceptorIndex; i >= 0; i--) {
      HandshakeInterceptor interceptor = this.interceptors.get(i);
      try {
        interceptor.afterHandshake(context, this.wsHandler, failure);
      } catch (Throwable ex) {
        if (logger.isWarnEnabled()) {
          logger.warn(interceptor + " threw exception in afterHandshake: " + ex);
        }
      }
    }
  }

}
