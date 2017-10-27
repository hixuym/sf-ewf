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

package io.sunflower.ewf.internal.template;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.Router;
import io.sunflower.ewf.internal.RouterImpl;
import io.sunflower.ewf.support.Settings;
import io.sunflower.ewf.i18n.Lang;
import io.sunflower.ewf.i18n.internal.LangImpl;
import io.sunflower.ewf.spi.TemplateEngine;
import io.sunflower.ewf.spi.internal.TemplateEngineJson;
import io.sunflower.ewf.spi.internal.TemplateEngineJsonP;
import io.sunflower.ewf.spi.internal.TemplateEngineText;
import io.sunflower.guice.LoggerProvider;
import org.junit.Test;
import org.slf4j.Logger;

public class TemplateEngineManagerImplTest {

  @Test
  public void testGetJson() {
    assertThat(createTemplateEngineManager().getTemplateEngineForContentType(
        Result.APPLICATION_JSON), instanceOf(TemplateEngineJson.class));
  }

  @Test
  public void testGetJsonP() {
    assertThat(createTemplateEngineManager().getTemplateEngineForContentType(
        Result.APPLICATION_JSONP), instanceOf(TemplateEngineJsonP.class));
  }

  @Test
  public void testGetCustom() {
    assertThat(
        createTemplateEngineManager(CustomTemplateEngine.class).getTemplateEngineForContentType(
            "custom"), instanceOf(CustomTemplateEngine.class));
  }

  @Test
  public void testOverrideJson() {
    assertThat(createTemplateEngineManager(OverrideJsonTemplateEngine.class)
        .getTemplateEngineForContentType(
            Result.APPLICATION_JSON), instanceOf(OverrideJsonTemplateEngine.class));
  }

  @Test
  public void testOverrideHtml() {
    assertThat(createTemplateEngineManager(OverrideHtmlTemplateEngine.class)
        .getTemplateEngineForContentType(
            Result.TEXT_HTML), instanceOf(OverrideHtmlTemplateEngine.class));
  }

  @Test
  public void testOverrideHtmlOrderMatters() {
    TemplateEngineManager templateEngineManager
        = createTemplateEngineManager(
        OverrideHtmlTemplateEngine.class,
        OverrideHtmlTemplateEngine3.class,
        OverrideHtmlTemplateEngine2.class);

    assertThat(templateEngineManager.getTemplateEngineForContentType(
        Result.TEXT_HTML), instanceOf(OverrideHtmlTemplateEngine2.class));
  }

  @Test
  public void testContentTypes() {
    List<String> types = Lists.newArrayList(createTemplateEngineManager().getContentTypes());
    Collections.sort(types);
    assertThat(types.toString(),
        equalTo("[application/javascript, application/json, text/plain]"));
  }

  @Test
  public void testGetNonExistingProducesNoNPE() {
    TemplateEngineManager manager = createTemplateEngineManager(OverrideJsonTemplateEngine.class);
    // return default json engine.
    assertNull(manager.getTemplateEngineForContentType("non/existing"));
  }

  public static abstract class MockTemplateEngine implements TemplateEngine {

    @Override
    public void invoke(Context context, Result result) {

    }

    @Override
    public String getSuffixOfTemplatingEngine() {
      return null;

    }
  }

  public static class CustomTemplateEngine extends MockTemplateEngine {

    @Override
    public String getContentType() {
      return "custom";
    }
  }

  public static class OverrideJsonTemplateEngine extends MockTemplateEngine {

    @Override
    public String getContentType() {
      return Result.APPLICATION_JSON;
    }
  }

  public static class OverrideHtmlTemplateEngine extends MockTemplateEngine {

    @Override
    public String getContentType() {
      return Result.TEXT_HTML;
    }
  }

  public static class OverrideHtmlTemplateEngine2 extends MockTemplateEngine {

    @Override
    public String getContentType() {
      return Result.TEXT_HTML;
    }
  }

  public static class OverrideHtmlTemplateEngine3 extends MockTemplateEngine {

    @Override
    public String getContentType() {
      return Result.TEXT_HTML;
    }
  }

  private TemplateEngineManager createTemplateEngineManager(final Class<?>... toBind) {
    return createInjector(toBind).getInstance(TemplateEngineManager.class);
  }

  private Injector createInjector(final Class<?>... toBind) {
    return Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {

        bind(Logger.class).toProvider(LoggerProvider.class);
        bind(Lang.class).to(LangImpl.class);
        bind(Router.class).to(RouterImpl.class);

        bind(TemplateEngineText.class);
        bind(TemplateEngineJson.class);
        bind(TemplateEngineJsonP.class);

        bind(Settings.class).toInstance(new Settings());

        for (Class<?> clazz : toBind) {

          bind(clazz);

        }
      }
    });
  }
}
