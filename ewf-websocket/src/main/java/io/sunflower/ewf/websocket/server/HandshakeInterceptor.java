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

package io.sunflower.ewf.websocket.server;


import io.sunflower.ewf.Context;
import io.sunflower.ewf.websocket.WebSocketHandler;

import java.util.Map;

/**
 * Interceptor for WebSocket handshake requests. Can be used to inspect the handshake request and
 * response as well as to pass attributes to the target {@link WebSocketHandler}.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */

public interface HandshakeInterceptor {

    /**
     * Invoked before the handshake is processed.
     *
     * @param context    the current context
     * @param wsHandler  the target WebSocket handler
     * @param attributes attributes from the HTTP handshake to associate with the WebSocket session;
     *                   the provided attributes are copied, the original map is not used.
     * @return whether to proceed with the handshake ({@code true}) or abort ({@code false})
     */
    boolean beforeHandshake(Context context,
                            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception;

    /**
     * Invoked after the handshake is done. The response status and headers indicate the results of
     * the handshake, i.e. whether it was successful or not.
     *
     * @param context   the current context
     * @param wsHandler the target WebSocket handler
     * @param exception an exception raised during the handshake, or {@code null} if none
     */
    void afterHandshake(Context context, WebSocketHandler wsHandler, Exception exception);

}
