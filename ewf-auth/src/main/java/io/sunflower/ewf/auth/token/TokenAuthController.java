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

import io.sunflower.ewf.Context;
import io.sunflower.ewf.FilterWith;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.auth.UsernamePasswordValidator;
import io.sunflower.ewf.errors.BadRequestException;
import io.sunflower.ewf.spi.ExceptionHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.sunflower.ewf.Results.json;

/**
 * TokenAuthController
 *
 * @author michael created on 17/10/27 16:16
 */
@Singleton
public class TokenAuthController {

    @Inject
    private TokenManager tokenManager;

    @Inject
    private ExceptionHandler exceptionHandler;

    @Inject
    private UsernamePasswordValidator validator;

    public Result auth(Context context) {

        String username = context.getParameter("username");
        String password = context.getParameter("password");

        if (validator.validateCredentials(username, password)) {
            return json().render(tokenManager.generate(username));
        }

        return json().render("invalid username or password.");
    }

    @FilterWith(TokenAuthFilter.class)
    public Result refresh(@Token String token) {

        try {
            return json().render(tokenManager.refresh(token));
        } catch (TokenVerificationException e) {
            throw new BadRequestException(e.getMessage(), e);
        }

    }


}
