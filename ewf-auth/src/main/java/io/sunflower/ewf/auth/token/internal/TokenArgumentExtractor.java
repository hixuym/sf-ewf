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

package io.sunflower.ewf.auth.token.internal;

import com.google.common.net.HttpHeaders;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.params.ArgumentExtractor;

import javax.annotation.Nullable;

import static io.sunflower.ewf.auth.Constant.ACCESS_TOKEN_PARAM;
import static io.sunflower.ewf.auth.Constant.TOKEN_AUTH_PREFIX;

/**
 * TokenArgumentExtractor
 *
 * @author michael created on 17/10/28 10:17
 */
public class TokenArgumentExtractor implements ArgumentExtractor<String> {

    @Override
    public String extract(Context context) {

        String token = getToken(context.getHeader(HttpHeaders.AUTHORIZATION));

        // If Authorization header is not used, check query parameter where token can be passed as well
        if (token == null) {
            token = context.getParameter(ACCESS_TOKEN_PARAM);
        }

        return token;
    }

    @Override
    public Class<String> getExtractedType() {
        return String.class;
    }

    @Override
    public String getFieldName() {
        return null;
    }

    /**
     * Parses a value of the `Authorization` header in the form of `Bearer a892bf3e284da9bb40648ab10`.
     *
     * @param header the value of the `Authorization` header
     * @return a token
     */
    @Nullable
    private String getToken(String header) {
        if (header == null) {
            return null;
        }

        final int space = header.indexOf(' ');
        if (space <= 0) {
            return null;
        }

        final String method = header.substring(0, space);
        if (!TOKEN_AUTH_PREFIX.equalsIgnoreCase(method)) {
            return null;
        }

        return header.substring(space + 1);
    }

}
