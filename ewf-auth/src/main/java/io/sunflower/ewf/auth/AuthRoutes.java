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

import io.sunflower.ewf.ApplicationRoutes;
import io.sunflower.ewf.Router;
import io.sunflower.ewf.auth.token.TokenAuthController;

/**
 * AuthRoutes
 *
 * @author michael created on 17/10/28 10:14
 */
public class AuthRoutes implements ApplicationRoutes {

    @Override
    public void init(Router router) {

        Router sub = router.subRouter("/api/auth");

        sub.POST().route("/get_token")
                .ignoreGlobalFilters().with(TokenAuthController::auth);

        sub.POST().route("/refresh_token")
                .with(TokenAuthController::refresh);

    }
}
