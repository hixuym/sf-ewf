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

import com.google.common.net.HttpHeaders;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Filter;
import io.sunflower.ewf.FilterChain;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.auth.UsernamePasswordValidator;
import io.sunflower.ewf.spi.ExceptionHandler;
import io.sunflower.ewf.support.Constants;
import org.apache.commons.codec.binary.Base64;

import javax.inject.Inject;
import java.nio.charset.Charset;

import static io.sunflower.ewf.auth.Constant.*;

/**
 * A Ninja filter that implements HTTP BASIC Authentication.
 *
 * @author James Moger
 */
public class BasicAuthFilter implements Filter {

    protected final ExceptionHandler exceptionHandler;

    protected final UsernamePasswordValidator credentialsValidator;

    protected final String challenge;

    @Inject
    public BasicAuthFilter(ExceptionHandler exceptionHandler,
                           UsernamePasswordValidator validator) {
        this.exceptionHandler = exceptionHandler;
        this.credentialsValidator = validator;
        this.challenge = String.format(CHALLENGE_FORMAT, BASIC_AUTH_PREFIX, DEFAULT_REALM_KEY);
    }

    @Override
    public Result filter(FilterChain chain, Context context) {

        if (context.getSession() == null) {
            // no session
            return exceptionHandler.getUnauthorizedResult(context);

        } else if (context.getSession().get(USERNAME_KEY) == null) {
            // no login, conditionally challenge
            String header = context.getHeader(HttpHeaders.AUTHORIZATION);

            if (header != null && header.startsWith(BASIC_AUTH_PREFIX)) {
                // Authorization: Basic BASE64PACKET
                String packet = header.substring(BASIC_AUTH_PREFIX.length()).trim();
                String credentials = new String(Base64.decodeBase64(packet),
                        Charset.forName(Constants.UTF_8));

                // credentials = username:password
                final String[] values = credentials.split(":", 2);
                final String username = values[0];
                final String password = values[1];

                if (credentialsValidator
                        .validateCredentials(username, password)) {

                    context.getSession().put(USERNAME_KEY, username);

                    return chain.next(context);
                }
            }

            return exceptionHandler.getUnauthorizedResult(context).addHeader(
                    Result.WWW_AUTHENTICATE, challenge);

        } else {
            return chain.next(context);
        }
    }
}
