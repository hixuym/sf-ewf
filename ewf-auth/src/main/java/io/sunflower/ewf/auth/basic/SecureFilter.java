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

package io.sunflower.ewf.auth.basic;

import com.google.common.base.Strings;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Filter;
import io.sunflower.ewf.FilterChain;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.spi.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.sunflower.ewf.auth.Constant.USERNAME_KEY;

/**
 * SecureFilter
 *
 * @author michael created on 17/10/27 15:58
 */
@Singleton
public class SecureFilter implements Filter {

    @Inject
    private ExceptionHandler exceptionHandler;

    @Override
    public Result filter(FilterChain filterChain, Context context) {

        String uid = context.getSession().get(USERNAME_KEY);

        if (!Strings.isNullOrEmpty(uid)) {
            return filterChain.next(context);
        }

        return exceptionHandler.getUnauthorizedResult(context);
    }
}
