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

package io.sunflower.ewf.websocket;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.websocket.handler.AbstractWebSocketHandler;
import io.sunflower.ewf.websocket.server.*;
import io.sunflower.ewf.websocket.server.support.DefaultHandshakeHandler;
import io.sunflower.ewf.websocket.server.support.DefaultRequestUpgradeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by michael on 16/12/7.
 */
public abstract class AbstractWebSocketController extends AbstractWebSocketHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Set<WebSocketSession> sessions =
            Collections.newSetFromMap(new ConcurrentHashMap<WebSocketSession, Boolean>());

    private final HandshakeHandler handshakeHandler;

    private final List<HandshakeInterceptor> interceptors = Lists.newArrayList();

    public AbstractWebSocketController() {
        RequestUpgradeStrategy requestUpgradeStrategy = new DefaultRequestUpgradeStrategy(sessions);
        this.handshakeHandler = new DefaultHandshakeHandler(requestUpgradeStrategy);
    }

    public AbstractWebSocketController(HandshakeHandler handshakeHandler) {
        this.handshakeHandler = handshakeHandler;
    }

    protected void addHandshakeInterceptor(HandshakeInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    public Result handshake(Context context) {

        WebSocketHandler handler = this;

        HandshakeInterceptorChain chain = new HandshakeInterceptorChain(this.interceptors, handler);

        HandshakeFailureException failure = null;

        try {
            if (logger.isDebugEnabled()) {
                logger.debug(context.getMethod() + " " + context.getRequestPath());
            }

            Map<String, Object> attributes = Maps.newHashMap();

            if (!chain.applyBeforeHandshake(context, attributes)) {
                return null;
            }

            try {
                return this.handshakeHandler.doHandshake(context, handler, attributes);
            } finally {
                chain.applyAfterHandshake(context, null);
                context.cleanup();
            }
        } catch (HandshakeFailureException ex) {
            failure = ex;
        } catch (Throwable ex) {
            failure = new HandshakeFailureException(
                    "Uncaught failure for request " + context.getRoute().getUri(), ex);
        } finally {
            if (failure != null) {
                chain.applyAfterHandshake(context, failure);
                throw failure;
            }
        }

        return null;
    }

    protected Set<WebSocketSession> getSessions() {
        return sessions;
    }
}
