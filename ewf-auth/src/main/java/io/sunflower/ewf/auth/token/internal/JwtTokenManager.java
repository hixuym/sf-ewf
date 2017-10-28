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

package io.sunflower.ewf.auth.token.internal;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.sunflower.ewf.auth.token.TokenManager;
import io.sunflower.ewf.auth.token.TokenVerificationException;
import io.sunflower.ewf.errors.InternalServerErrorException;
import io.sunflower.ewf.support.Settings;
import io.sunflower.util.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.time.Instant;

import static io.sunflower.ewf.auth.Constant.JWT_ISSUER;
import static io.sunflower.ewf.auth.Constant.JWT_UID_CLAIM_KEY;

/**
 * JwtTokenManager
 *
 * @author michael created on 17/10/27 16:16
 */
@Singleton
public class JwtTokenManager implements TokenManager {

    private final JWTVerifier verifier;
    private Algorithm algorithmHS;
    private final Duration tokenExpiredTime;

    @Inject
    public JwtTokenManager(Settings settings) {
        this.tokenExpiredTime = settings.getTokenExpireTime();

        try {
            algorithmHS = Algorithm.HMAC256(settings.getApplicationSecret());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        verifier = JWT.require(algorithmHS)
                .withIssuer(JWT_ISSUER)
                .build();
    }

    @Override
    public String generate(String uid) {
        try {
            long expiredTime = System.currentTimeMillis() + tokenExpiredTime.toMilliseconds();
            return JWT.create()
                    .withIssuer(JWT_ISSUER)
                    .withExpiresAt(Date.from(Instant.ofEpochMilli(expiredTime)))
                    .withClaim(JWT_UID_CLAIM_KEY, uid)
                    .sign(algorithmHS);
        } catch (JWTCreationException e) {
            throw new InternalServerErrorException("jwt creation error.", e);
        }
    }

    @Override
    public String refresh(String token) throws TokenVerificationException {

        String uid = verify(token);

        return generate(uid);
    }

    @Override
    public void delete(String token) {
        // do nothing
    }

    @Override
    public String verify(String token) throws TokenVerificationException {

        try {
            DecodedJWT jwt = verifier.verify(token);
            Claim uid = jwt.getClaim(JWT_UID_CLAIM_KEY);
            return uid.asString();
        } catch (Exception e) {
            throw new TokenVerificationException("invalid token.", e);
        }
    }
}
