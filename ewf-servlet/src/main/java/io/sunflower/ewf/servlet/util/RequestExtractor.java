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

package io.sunflower.ewf.servlet.util;

import io.sunflower.ewf.Context;
import io.sunflower.ewf.params.ArgumentExtractor;
import io.sunflower.ewf.servlet.ServletRequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * @author michael
 */
public class RequestExtractor implements ArgumentExtractor<HttpServletRequest> {

    @Override
    public HttpServletRequest extract(Context context) {

        if (context instanceof ServletRequestContext) {
            return ((ServletRequestContext) context).getHttpServletRequest();
        } else {
            throw new RuntimeException(
                    "RequestExtractor only works with Servlet container implementation of Context.");
        }

    }

    @Override
    public Class<HttpServletRequest> getExtractedType() {
        return HttpServletRequest.class;
    }

    @Override
    public String getFieldName() {
        return null;
    }
}