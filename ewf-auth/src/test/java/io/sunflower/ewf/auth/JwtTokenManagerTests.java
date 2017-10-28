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

import io.sunflower.ewf.auth.token.internal.JwtTokenManager;
import io.sunflower.ewf.support.Settings;
import org.junit.Test;

/**
 * JwtTokenManagerTests
 *
 * @author michael created on 17/10/26 17:15
 */
public class JwtTokenManagerTests {

    @Test
    public void testJwtToken() throws Exception {

        JwtTokenManager tokenManager = new JwtTokenManager(new Settings());

        String token = tokenManager.generate("michael");

        System.out.println(token);

        String uid = tokenManager.verify(token);

        System.out.println(uid);

        String refreshToken = tokenManager.refresh(token);

        System.out.println(refreshToken);
    }

}
