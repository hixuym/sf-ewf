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

import io.sunflower.ewf.Result;
import io.sunflower.ewf.internal.Route;
import io.sunflower.ewf.support.Constants;

/**
 * Helper methods for template engines
 *
 * @author James Roper
 */
public class TemplateEngineHelper {

    public String getTemplateForResult(Route route, Result result, String suffix) {
        if (result.getTemplate() == null) {
            Class controllerClass = route.getControllerClass();

            // Calculate the correct path of the template.
            // We always assume the template in the subdir "views"

            // 1) If we are in the main project =>
            // /controllers/ControllerName
            // to
            // /views/ControllerName/templateName.ftl.html
            // 2) If we are in a plugin / subproject
            // =>
            // /controllers/some/packages/submoduleName/ControllerName
            // to
            // views/some/packages/submoduleName/ControllerName/templateName.ftl.html

            // So let's calculate the parent package of the resource:
            String controllerPackageName = controllerClass.getPackage().getName();
            // This results in something like resources or
            // some.package.resources

            // Replace resource prefix with views prefix
            String parentPackageOfResource = controllerPackageName
                    .replaceFirst(Constants.CONTROLLERS_DIR, Constants.VIEWS_DIR);

            // And now we rewrite everything from "." notation to directories /
            String parentResourcePackageAsPath = parentPackageOfResource
                    .replaceAll("\\.", "/");

            // and the final path of the controller will be something like:
            // views/some/package/submoduleName/ResourceName/templateName.ftl.html
            return String.format("/%s/%s/%s%s", parentResourcePackageAsPath,
                    controllerClass.getSimpleName(), route.getControllerMethod().getName(), suffix);
        } else {
            return result.getTemplate();
        }
    }

}
