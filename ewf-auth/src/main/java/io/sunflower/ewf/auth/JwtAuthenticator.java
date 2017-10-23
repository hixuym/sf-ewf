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

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Date;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.sunflower.ewf.Settings;

/**
 * JwtAuthenticator
 *
 * @author michael created on 17/10/21 22:10
 */
@Singleton
public class JwtAuthenticator<P extends Principal> implements Authenticator<String, P> {

  private final JWTVerifier verifier;

  @Inject
  public JwtAuthenticator(Settings settings) {
    try {
      Algorithm algorithmHS = Algorithm.HMAC256(settings.getApplicationSecret());

      verifier = JWT.require(algorithmHS)
          .withIssuer("ewf")
          .build(); //Reusable verifier instance

    } catch (UnsupportedEncodingException e) {
      throw new AuthenticationException("unsupported algorithm of hmac256", e);
    }
  }

  @Override
  public Optional<P> authenticate(String credentials) throws AuthenticationException {

    try {
      DecodedJWT jwt = verifier.verify(credentials);

      Date expireAt = jwt.getExpiresAt();

      // 过期
      if (expireAt != null && expireAt.before(new Date())) {
        throw new AuthenticationException("expired token");
      }

      Claim claim = jwt.getClaim("uid");

      if (!claim.isNull()) {

        Principal principal = new PrincipalImpl(claim.asString());

        return (Optional<P>) Optional.of(principal);
      }

    } catch (Throwable t) {
      throw new AuthenticationException("invalid token", t);
    }

    return Optional.empty();
  }

}
