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

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.sunflower.ewf.assets.AssetsResource;

/**
 * @author michael
 */
@Singleton
public class TemplateEngineFreemarkerAssetsAtMethod implements
    TemplateMethodModelEx {

  private final TemplateEngineFreemarkerReverseRouteHelper templateEngineFreemarkerReverseRouteHelper;

  @Inject
  public TemplateEngineFreemarkerAssetsAtMethod(
      TemplateEngineFreemarkerReverseRouteHelper templateEngineFreemarkerReverseRouteHelper) {
    this.templateEngineFreemarkerReverseRouteHelper = templateEngineFreemarkerReverseRouteHelper;

  }

  @Override
  public TemplateModel exec(List args) throws TemplateModelException {

    List<Object> argsWithResourceAndMethod = new ArrayList<>(args.size() + 2);
    argsWithResourceAndMethod.add(AssetsResource.class.getName());
    argsWithResourceAndMethod.add("serveStatic");
    argsWithResourceAndMethod.add("fileName");
    argsWithResourceAndMethod.addAll(args);

    return templateEngineFreemarkerReverseRouteHelper
        .computeReverseRoute(argsWithResourceAndMethod);

  }
}
