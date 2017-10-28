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

package io.sunflower.ewf.auth.internal;

import io.sunflower.ewf.Context;
import io.sunflower.ewf.params.ArgumentExtractor;

import static io.sunflower.ewf.auth.Constant.USERNAME_KEY;

/**
 * UsernameArgumentExtractor
 *
 * @author michael created on 17/10/27 14:37
 */
public class UsernameArgumentExtractor implements ArgumentExtractor<String> {

    @Override
    public String extract(Context context) {

        String uid = context.getSession().get(USERNAME_KEY);

        if (uid == null) {
            uid = (String) context.getAttribute(USERNAME_KEY);
        }

        return uid;
    }

    @Override
    public Class<String> getExtractedType() {
        return String.class;
    }

    @Override
    public String getFieldName() {
        return null;
    }
}
