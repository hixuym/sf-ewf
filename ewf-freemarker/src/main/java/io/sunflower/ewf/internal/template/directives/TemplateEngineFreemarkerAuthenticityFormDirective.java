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

package io.sunflower.ewf.internal.template.directives;

import freemarker.core.Environment;
import freemarker.template.*;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.support.Constants;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;


/**
 * @author michael
 */
@SuppressWarnings("rawtypes")
public class TemplateEngineFreemarkerAuthenticityFormDirective implements TemplateDirectiveModel {

    private Context context;

    public TemplateEngineFreemarkerAuthenticityFormDirective(Context context) {
        this.context = context;
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
                        TemplateDirectiveBody body) throws TemplateException, IOException {
        if (!params.isEmpty()) {
            throw new TemplateModelException("This directive doesn't allow parameters.");
        }

        if (loopVars.length != 0) {
            throw new TemplateModelException("This directive doesn't allow loop variables.");
        }

        Writer out = env.getOut();
        out.append("<input type=\"hidden\" value=\"")
                .append(context.getSession().getAuthenticityToken())
                .append("\" name=\"")
                .append(Constants.AUTHENTICITY_TOKEN)
                .append("\" />");
    }
}