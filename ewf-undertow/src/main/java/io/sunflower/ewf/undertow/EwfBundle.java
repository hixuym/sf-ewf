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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.sunflower.Configuration;
import io.sunflower.ewf.WebApplicationModule;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Router;
import io.sunflower.ewf.Settings;
import io.sunflower.ewf.application.ApplicationRoutes;
import io.sunflower.guicey.Injectors;
import io.sunflower.setup.Bootstrap;
import io.sunflower.setup.Environment;
import io.sunflower.undertow.UndertowBundle;
import io.sunflower.undertow.UndertowModule;

public class EwfBundle<T extends Configuration> extends UndertowBundle<T> {

  @Override
  public void initialize(Bootstrap<?> bootstrap) {
  }

  @Override
  protected void configure(T configuration, Environment environment,
      UndertowModule undertowModule) {

    environment.addServerLifecycleListener(server -> {
      Injector injector = server.getInjector();

      Set<ApplicationRoutes> routesSet =
          Injectors.instanceOf(injector, ApplicationRoutes.class);

      List<ApplicationRoutes> routesList = Lists.newArrayList(routesSet);

      Collections.sort(routesList);

      Router router = injector.getInstance(Router.class);

      for (ApplicationRoutes routes : routesList) {
        routes.init(router);
      }

      router.compileRoutes();
    });

    Settings settings = new Settings(configuration.getServerFactory().getServerProperties());
    environment.guicey().registry(settings);

    undertowModule
        .registryApplicationHandler(settings.getHandlerPath(), EwfHttpHandlerProvider.class);

    WebApplicationModule webApplicationModule = new WebApplicationModule() {

      @Override
      protected Class<? extends Context> getRequestContextImpl() {
        return settings.isSessionEnabled() ?
            UndertowWithSessionContext.class : UndertowContext.class;
      }
    };

    configure(webApplicationModule);
    environment.guicey().registry(webApplicationModule);

  }

  protected void configure(WebApplicationModule webApplicationModule) {

  }
}
