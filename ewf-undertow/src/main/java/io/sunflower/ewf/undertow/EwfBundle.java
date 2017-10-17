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

import static io.sunflower.undertow.handler.Handlers.BLOCKING_WRAPPER;

import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.multibindings.MapBinder;
import io.sunflower.Configuration;
import io.sunflower.ConfiguredBundle;
import io.sunflower.ewf.BasicModule;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Router;
import io.sunflower.ewf.Settings;
import io.sunflower.ewf.application.ApplicationRoutes;
import io.sunflower.guicey.Injectors;
import io.sunflower.setup.Bootstrap;
import io.sunflower.setup.Environment;
import io.sunflower.undertow.AppHandlers;
import io.sunflower.undertow.UndertowModule;
import io.undertow.server.HttpHandler;

public class EwfBundle<T extends Configuration> implements ConfiguredBundle<T> {

  @Override
  public void run(T configuration, Environment environment) throws Exception {

    environment.addServerLifecycleListener(server -> {
      Injector injector = server.getInjector();

      Set<ApplicationRoutes> routesSet =
          Injectors.instanceOf(injector, ApplicationRoutes.class);

      Router router = injector.getInstance(Router.class);

      for (ApplicationRoutes routes : routesSet) {
        routes.init(router);
      }

      router.compileRoutes();
    });

    environment.guicey().registry(new UndertowModule(environment));
    environment.guicey().registry(new BasicModule());

    Settings settings = new Settings(configuration.getServerFactory().getServerProperties());
    environment.guicey().registry(settings);

    environment.guicey().registry(new AbstractModule() {
      @Override
      protected void configure() {
        MapBinder<String, HttpHandler> mapBinder =
            MapBinder.newMapBinder(binder(), String.class, HttpHandler.class, AppHandlers.class);

        EwfHttpHandler ewfHttpHandler = new EwfHttpHandler();
        requestInjection(ewfHttpHandler);

        mapBinder.addBinding(settings.getHandlerPath())
            .toInstance(BLOCKING_WRAPPER.wrap(ewfHttpHandler));

        bind(Context.class).to(UndertowContext.class);
      }
    });
  }

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
  }

}
