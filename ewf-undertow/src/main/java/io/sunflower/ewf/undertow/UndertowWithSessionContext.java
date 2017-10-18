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

import javax.inject.Inject;

import com.google.inject.Injector;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.Settings;
import io.sunflower.ewf.bodyparser.BodyParserEngineManager;
import io.sunflower.ewf.params.ParamParsers;
import io.sunflower.ewf.session.FlashScope;
import io.sunflower.ewf.session.Session;
import io.sunflower.ewf.utils.ResponseStreams;
import io.sunflower.ewf.validation.Validation;
import io.undertow.server.HttpServerExchange;

/**
 * UndertowWithSessionContext
 *
 * @author michael created on 17/10/18 10:41
 */
public class UndertowWithSessionContext extends UndertowContext {

  private final FlashScope flashScope;
  private final Session session;

  @Inject
  public UndertowWithSessionContext(
      BodyParserEngineManager bodyParserEngineManager,
      Settings configuration,
      Validation validation, Injector injector,
      ParamParsers paramParsers,
      FlashScope flashScope,
      Session session) {
    super(bodyParserEngineManager, configuration, validation, injector, paramParsers);

    this.flashScope = flashScope;
    this.session = session;
  }


  @Override
  public FlashScope getFlashScope() {
    return this.flashScope;
  }

  @Override
  public Session getSession() {
    return this.session;
  }

  @Override
  protected ResponseStreams finalizeHeaders(Result result, Boolean handleFlashAndSessionCookie) {
    // copy flash and session data directory to this context
    if (handleFlashAndSessionCookie) {
      flashScope.save(this);
      session.save(this);
    }

    return super.finalizeHeaders(result, handleFlashAndSessionCookie);
  }

  @Override
  public void init(HttpServerExchange exchange, String contextPath) {

    super.init(exchange, contextPath);

    flashScope.init(this);
    session.init(this);

  }
}
