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

package io.sunflower.ewf.internal.bodyparser;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import io.sunflower.ewf.Router;
import io.sunflower.ewf.i18n.Lang;
import io.sunflower.ewf.i18n.internal.LangImpl;
import io.sunflower.ewf.internal.RouterImpl;
import io.sunflower.ewf.params.ParamParser;
import io.sunflower.ewf.spi.internal.BodyParserEngineJson;
import io.sunflower.ewf.spi.internal.BodyParserEnginePost;
import io.sunflower.ewf.support.Settings;
import io.sunflower.guice.LoggerProvider;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class BodyParserEngineManagerImplTest {

    @Test
    public void testContentTypes() {
        List<String> types = Lists.newArrayList(createBodyParserEngineManager().getContentTypes());
        Collections.sort(types);
        assertThat(types.toString(),
                equalTo("[application/json, application/x-www-form-urlencoded]"));
    }

    private BodyParserEngineManager createBodyParserEngineManager(final Class<?>... toBind) {
        return createInjector(toBind).getInstance(BodyParserEngineManager.class);
    }

    private Injector createInjector(final Class<?>... toBind) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                bind(Logger.class).toProvider(LoggerProvider.class);
                bind(Lang.class).to(LangImpl.class);

                Multibinder.newSetBinder(binder(), ParamParser.class);
                bind(Router.class).to(RouterImpl.class);

                bind(BodyParserEnginePost.class);
                bind(BodyParserEngineJson.class);

                bind(Settings.class).toInstance(new Settings());

                for (Class<?> clazz : toBind) {

                    bind(clazz);

                }
            }
        });
    }
}
