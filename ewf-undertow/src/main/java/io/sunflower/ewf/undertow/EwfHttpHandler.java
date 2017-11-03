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
package io.sunflower.ewf.undertow;

import com.google.inject.Injector;
import io.sunflower.ewf.internal.ValidationImpl;
import io.sunflower.ewf.internal.bodyparser.BodyParserEngineManager;
import io.sunflower.ewf.params.internal.ParamParsers;
import io.sunflower.ewf.session.internal.FlashScopeImpl;
import io.sunflower.ewf.session.internal.SessionImpl;
import io.sunflower.ewf.session.internal.support.Clock;
import io.sunflower.ewf.session.internal.support.CookieEncryption;
import io.sunflower.ewf.session.internal.support.Crypto;
import io.sunflower.ewf.spi.RequestHandler;
import io.sunflower.ewf.support.Settings;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a request from Undertow and then delegates to ewf.
 *
 * @author michael
 */
public class EwfHttpHandler implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(EwfHttpHandler.class);

    private final Settings settings;
    private final RequestHandler requestHandler;
    private final BodyParserEngineManager bodyParserEngineManager;
    private final ParamParsers paramParsers;

    private final Crypto crypto;
    private final CookieEncryption cookieEncryption;
    private final Clock clock;

    public EwfHttpHandler(Injector injector) {
        this.settings = injector.getInstance(Settings.class);
        this.requestHandler = injector.getInstance(RequestHandler.class);
        this.bodyParserEngineManager = injector.getInstance(BodyParserEngineManager.class);
        this.paramParsers = injector.getInstance(ParamParsers.class);

        this.crypto = injector.getInstance(Crypto.class);
        this.clock = injector.getInstance(Clock.class);
        this.cookieEncryption = injector.getInstance(CookieEncryption.class);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // create compatible context element
        UndertowContext undertowContext =
                new UndertowContext(
                        bodyParserEngineManager,
                        settings,
                        new ValidationImpl(),
                        paramParsers,
                        new FlashScopeImpl(settings),
                        new SessionImpl(crypto, cookieEncryption, settings, clock));

        // initialize it
        undertowContext.init(exchange, settings.getContextPath());

        // invoke requestHandler on it
        requestHandler.handleRequest(undertowContext);
    }

}
