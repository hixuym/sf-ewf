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

package io.sunflower.ewf;

import java.util.Arrays;
import java.util.List;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.sunflower.ewf.bodyparser.BodyParserEngineJson;
import io.sunflower.ewf.bodyparser.BodyParserEnginePost;
import io.sunflower.ewf.params.ParamParser;
import io.sunflower.ewf.template.TemplateEngineJson;
import io.sunflower.ewf.template.TemplateEngineJsonP;
import io.sunflower.ewf.template.TemplateEngineText;

/**
 * BasicEwfModule
 *
 * @author michael created on 17/10/17 17:33
 */
public abstract class BasicEwfModule extends AbstractModule {

  private final List<Class<? extends ParamParser>> paramParsers = Lists.newArrayList();

  @Override
  protected void configure() {
    // Routing
    Multibinder<ParamParser> multibinder
        = Multibinder.newSetBinder(binder(), ParamParser.class);

    for (Class<? extends ParamParser> parser : paramParsers) {
      multibinder.addBinding().to(parser);
    }

    bind(RouteBuilder.class).to(RouteBuilderImpl.class);
    bind(Router.class).to(RouterImpl.class).in(Singleton.class);

    bind(BodyParserEnginePost.class);
    bind(BodyParserEngineJson.class);

    bind(TemplateEngineJson.class);
    bind(TemplateEngineJsonP.class);
    bind(TemplateEngineText.class);

    bind(Context.class).to(getRequestContextImpl());
  }

  @SafeVarargs
  public final void registryParamParser(Class<? extends ParamParser>... parser) {
    this.paramParsers.addAll(Arrays.asList(parser));
  }

  protected abstract Class<? extends Context> getRequestContextImpl();
}
