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

package io.sunflower.ewf.auth.oauth;


import java.security.Principal;
import javax.annotation.Nullable;

import com.google.common.net.HttpHeaders;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.FilterChain;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.SecurityContext;
import io.sunflower.ewf.auth.AbstractAuthFilter;

/**
 * @author michael
 */
public class OAuthCredentialAuthFilter<P extends Principal> extends AbstractAuthFilter<String, P> {

  /**
   * Query parameter used to pass Bearer token
   *
   * @see <a href="https://tools.ietf.org/html/rfc6750#section-2.3">The OAuth 2.0 Authorization
   * Framework: Bearer Token Usage</a>
   */
  public static final String OAUTH_ACCESS_TOKEN_PARAM = "access_token";

  private OAuthCredentialAuthFilter() {
  }

  @Override
  public Result filter(FilterChain chain, final Context requestContext) {
    String credentials = getCredentials(requestContext.getHeader(HttpHeaders.AUTHORIZATION));

    // If Authorization header is not used, check query parameter where token can be passed as well
    if (credentials == null) {
      credentials = requestContext.getParameter(OAUTH_ACCESS_TOKEN_PARAM);
    }

    if (!authenticate(requestContext, credentials, SecurityContext.BASIC_AUTH)) {
      return (unauthorizedHandler.onUnauthorized(prefix, realm));
    }

    return chain.next(requestContext);
  }

  /**
   * Parses a value of the `Authorization` header in the form of `Bearer
   * a892bf3e284da9bb40648ab10`.
   *
   * @param header the value of the `Authorization` header
   * @return a token
   */
  @Nullable
  private String getCredentials(String header) {
    if (header == null) {
      return null;
    }

    final int space = header.indexOf(' ');
    if (space <= 0) {
      return null;
    }

    final String method = header.substring(0, space);
    if (!prefix.equalsIgnoreCase(method)) {
      return null;
    }

    return header.substring(space + 1);
  }
}
