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

package io.sunflower.ewf.undertow.support;

import com.google.inject.Injector;
import io.sunflower.ewf.support.Constants;
import io.sunflower.ewf.undertow.EwfHttpHandler;
import io.sunflower.lifecycle.setup.StandardThreadExecutor;
import io.sunflower.undertow.handler.StandardThreadExecutorBlockingHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormParserFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * EwfHttpHandlerProvider
 *
 * @author michael created on 17/10/18 13:03
 */
@Singleton
public class EwfHttpHandlerProvider implements Provider<HttpHandler> {

    private final Injector injector;
    private final StandardThreadExecutor executor;

    @Inject
    public EwfHttpHandlerProvider(Injector injector) {
        this.injector = injector;
        this.executor = injector.getInstance(StandardThreadExecutor.class);
    }

    @Override
    public HttpHandler get() {
        HttpHandler h = new EwfHttpHandler(injector);
        // then eagerly parse form data (which is then included as an attachment)
        FormParserFactory.Builder formParserFactoryBuilder = FormParserFactory.builder();
        formParserFactoryBuilder.setDefaultCharset(Constants.UTF_8);
        h = new EagerFormParsingHandler(formParserFactoryBuilder.build()).setNext(h);

        return new StandardThreadExecutorBlockingHandler(executor, h);
    }
}
