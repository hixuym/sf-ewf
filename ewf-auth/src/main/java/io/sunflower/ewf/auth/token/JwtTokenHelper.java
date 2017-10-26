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

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.sunflower.ewf.Settings;
import io.sunflower.ewf.auth.AuthenticationException;

/**
 * JwtTokenHelper
 *
 * @author michael created on 17/10/26 15:37
 */
@Singleton
public class JwtTokenHelper {

  private final JWTVerifier verifier;
  private Algorithm algorithmHS;

  @Inject
  public JwtTokenHelper(Settings settings) {
    try {
      algorithmHS = Algorithm.HMAC256(settings.getApplicationSecret());
    } catch (UnsupportedEncodingException e) {
      throw new AuthenticationException(e);
    }

    verifier = JWT.require(algorithmHS)
        .withIssuer("ewf")
        .build();
  }

  public Map<String, Claim> verify(String token) {
    DecodedJWT jwt = verifier.verify(token);
    return jwt.getClaims();
  }

  public Optional<String> extract(String token) {
    Map<String, Claim> claims = verify(token);

    return Optional.ofNullable(claims.get("username").asString());
  }

  public String buildToken(String username, Date expiredAt) {
    return JWT.create()
        .withIssuer("ewf")
        .withExpiresAt(expiredAt)
        .withClaim("username", username)
        .sign(algorithmHS);
  }

  public String buildToken(String username) {
    return JWT.create()
        .withIssuer("ewf")
        .withClaim("username", username)
        .sign(algorithmHS);
  }

  public void setAlgorithm(Algorithm algorithmHS) {
    this.algorithmHS = algorithmHS;
  }
}
