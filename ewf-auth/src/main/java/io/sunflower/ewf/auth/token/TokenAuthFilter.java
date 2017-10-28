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

package io.sunflower.ewf.auth.token;

import com.google.common.base.Strings;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Filter;
import io.sunflower.ewf.FilterChain;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.auth.token.internal.TokenArgumentExtractor;
import io.sunflower.ewf.spi.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.sunflower.ewf.auth.Constant.USERNAME_KEY;

/**
 * TokenAuthFilter
 *
 * @author michael created on 17/10/27 14:40
 */
@Singleton
public class TokenAuthFilter implements Filter {

    @Inject
    private TokenManager tokenManager;

    @Inject
    private ExceptionHandler exceptionHandler;

    private TokenArgumentExtractor tokenArgumentExtractor = new TokenArgumentExtractor();

    @Override
    public Result filter(FilterChain filterChain, Context context) {
        String token = tokenArgumentExtractor.extract(context);

        if (Strings.isNullOrEmpty(token)) {
            return exceptionHandler.getUnauthorizedResult(context);
        }

        try {
            context.setAttribute(USERNAME_KEY, tokenManager.verify(token));
        } catch (TokenVerificationException e) {
            // token verify failure.
            return exceptionHandler.getUnauthorizedResult(context);
        }

        return filterChain.next(context);
    }

}
