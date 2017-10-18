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
import io.sunflower.ewf.Context;
import io.sunflower.ewf.RouteHandler;
import io.sunflower.ewf.Settings;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a request from Undertow and then delegates to ewf.
 */
public class EwfHttpHandler implements HttpHandler {

  private static final Logger log = LoggerFactory.getLogger(EwfHttpHandler.class);

  private final Injector injector;
  private final Settings settings;
  private final RouteHandler routeHandler;

  public EwfHttpHandler(Injector injector, Settings settings,
      RouteHandler routeHandler) {
    this.injector = injector;
    this.settings = settings;
    this.routeHandler = routeHandler;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    // create Ninja compatible context element
    UndertowContext undertowContext
        = (UndertowContext) injector.getProvider(Context.class).get();

    // initialize it
    undertowContext.init(exchange, settings.getContextPath());

    // invoke routeHandler on it
    routeHandler.handleRequest(undertowContext);
  }

}
