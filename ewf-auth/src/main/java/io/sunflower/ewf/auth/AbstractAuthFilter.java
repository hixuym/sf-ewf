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

import java.security.Principal;
import java.util.Optional;
import javax.inject.Inject;

import io.sunflower.ewf.Context;
import io.sunflower.ewf.ExceptionHandler;
import io.sunflower.ewf.Filter;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.SecurityContext;
import io.sunflower.ewf.errors.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michael
 */
public abstract class AbstractAuthFilter<C, P extends Principal> implements Filter {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private static final String CHALLENGE_FORMAT = "%s realm=\"%s\"";

  @Inject
  protected Authenticator<C, P> authenticator;

  @Inject
  protected Authorizer<P> authorizer;

  @Inject
  private ExceptionHandler exceptionHandler;

  protected Result getUnauthorizedResult(Context context) {
    return exceptionHandler.getUnauthorizedResult(context)
        .addHeader(Result.WWW_AUTHENTICATE, String.format(CHALLENGE_FORMAT, getPrefix(), getRealm()));
  }

  protected String getPrefix() {
    return "Basic";
  }

  protected String getRealm() {
    return "realm";
  }

  /**
   * Authenticates a request with user credentials and setup the security context.
   *
   * @param context the context of the request
   * @param credentials the user credentials
   * @param scheme the authentication scheme; one of {@code BASIC_AUTH, FORM_AUTH, CLIENT_CERT_AUTH,
   * DIGEST_AUTH}. See {@link SecurityContext}
   * @return {@code true}, if the request is authenticated, otherwise {@code false}
   */
  protected boolean authenticate(Context context, C credentials, String scheme) {
    try {
      if (credentials == null) {
        return false;
      }

      final Optional<P> principal = authenticator.authenticate(credentials);
      if (!principal.isPresent()) {
        return false;
      }

      final SecurityContext securityContext = context.getSecurityContext();

      context.setSecurityContext(new SecurityContext() {
        @Override
        public Principal getUserPrincipal() {
          return principal.get();
        }

        @Override
        public boolean isUserInRole(String role) {
          return authorizer.authorize(principal.get(), role);
        }

        @Override
        public boolean isSecure() {
          return "https".equalsIgnoreCase(context.getScheme());
        }

        @Override
        public String getAuthenticationScheme() {
          return scheme;
        }
      });
      return true;
    } catch (AuthenticationException e) {
      logger.warn("Error authenticating credentials", e);
      throw new InternalServerErrorException(e.getMessage(), e);
    }
  }
}
