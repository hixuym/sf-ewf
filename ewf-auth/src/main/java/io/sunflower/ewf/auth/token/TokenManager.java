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

import com.google.inject.ImplementedBy;
import io.sunflower.ewf.auth.token.internal.JwtTokenManager;

/**
 * TokenManager
 *
 * @author michael created on 17/10/27 14:41
 */
@ImplementedBy(JwtTokenManager.class)
public interface TokenManager {

    /**
     * generate user token.
     *
     * @param uid
     * @return
     */
    String generate(String uid);

    /**
     * extract user key by token
     *
     * @param token
     * @return uid
     * @throws TokenVerificationException
     */
    String verify(String token) throws TokenVerificationException;

    /**
     * refresh token base on valid pre token
     *
     * @param token
     * @return the refreshed token
     * @throws TokenVerificationException
     */
    String refresh(String token) throws TokenVerificationException;

    /**
     * invalid token
     *
     * @param token
     */
    void delete(String token);

}
