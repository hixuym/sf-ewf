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

package io.sunflower.ewf.auth;

import static io.sunflower.ewf.auth.UserManager.REQUEST_UID_KEY;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Filter;
import io.sunflower.ewf.FilterChain;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.spi.ExceptionHandler;

/**
 * TokenSecureFilter
 *
 * @author michael created on 17/10/27 14:40
 */
public class TokenSecureFilter implements Filter {

  /**
   * Query parameter used to pass Bearer token
   *
   * @see <a href="https://tools.ietf.org/html/rfc6750#section-2.3">The OAuth 2.0 Authorization Framework: Bearer Token Usage</a>
   */
  private static final String ACCESS_TOKEN_PARAM = "access_token";
  private static final String TOKEN_AUTH_PREFIX = "Bearer";

  @Inject
  private TokenManager tokenManager;

  @Inject
  private ExceptionHandler exceptionHandler;

  @Override
  public Result filter(FilterChain filterChain, Context context) {
    String token = getToken(context.getHeader(HttpHeaders.AUTHORIZATION));

    // If Authorization header is not used, check query parameter where token can be passed as well
    if (token == null) {
      token = context.getParameter(ACCESS_TOKEN_PARAM);
    }

    if (Strings.isNullOrEmpty(token)) {
      return exceptionHandler.getUnauthorizedResult(context);
    }

    try {
      context.setAttribute(REQUEST_UID_KEY, tokenManager.verify(token));
    } catch (TokenVerificationException e) {
      // token verify failure.
      return exceptionHandler.getUnauthorizedResult(context);
    }

    return filterChain.next(context);
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
