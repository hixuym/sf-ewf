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

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import javax.annotation.Nullable;

import com.google.common.io.BaseEncoding;
import com.google.common.net.HttpHeaders;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.FilterChain;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.SecurityContext;
import io.sunflower.ewf.auth.AbstractAuthFilter;

/**
 * @author michael
 */
public class BasicCredentialAuthFilter<P extends Principal>
    extends AbstractAuthFilter<BasicCredentials, P> {

  private BasicCredentialAuthFilter() {
  }

  @Override
  public Result filter(FilterChain chain, Context requestContext) {
    final BasicCredentials credentials =
        getCredentials(requestContext.getHeader(HttpHeaders.AUTHORIZATION));

    if (!authenticate(requestContext, credentials, SecurityContext.BASIC_AUTH)) {
      return unauthorizedHandler.onUnauthorized(prefix, realm);
    }

    return chain.next(requestContext);
  }

  /**
   * Parses a Base64-encoded value of the `Authorization` header in the form of `Basic
   * dXNlcm5hbWU6cGFzc3dvcmQ=`.
   *
   * @param header the value of the `Authorization` header
   * @return a username and a password as {@link BasicCredentials}
   */
  @Nullable
  private BasicCredentials getCredentials(String header) {
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

    final String decoded;
    try {
      decoded = new String(BaseEncoding.base64().decode(header.substring(space + 1)),
          StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      logger.warn("Error decoding credentials", e);
      return null;
    }

    // Decoded credentials is 'username:password'
    final int i = decoded.indexOf(':');
    if (i <= 0) {
      return null;
    }

    final String username = decoded.substring(0, i);
    final String password = decoded.substring(i + 1);
    return new BasicCredentials(username, password);
  }
}
