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

import java.security.Principal;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.sunflower.ewf.auth.AuthenticationException;
import io.sunflower.ewf.auth.Authenticator;
import io.sunflower.ewf.auth.PrincipalImpl;

/**
 * JwtTokenAuthenticator
 *
 * @author michael created on 17/10/21 22:10
 */
@Singleton
public class JwtTokenAuthenticator<P extends Principal> implements Authenticator<String, P> {

  private final JwtTokenHelper helper;

  @Inject
  public JwtTokenAuthenticator(JwtTokenHelper helper) {
    this.helper = helper;
  }

  @Override
  public Optional<P> authenticate(String credentials) throws AuthenticationException {

    try {
      Optional<String> username = helper.extract(credentials);

      return (Optional<P>) username.map(PrincipalImpl::new);

    } catch (Exception e) {
      throw new AuthenticationException(e.getMessage(), e);
    }

  }

}
