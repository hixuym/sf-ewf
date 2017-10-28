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

/**
 * Constant
 *
 * @author michael created on 17/10/28 13:00
 */
public interface Constant {

    /**
     * Query parameter used to pass Bearer token
     *
     * @see <a href="https://tools.ietf.org/html/rfc6750#section-2.3">The OAuth 2.0 Authorization Framework: Bearer Token Usage</a>
     */
    String ACCESS_TOKEN_PARAM = "access_token";

    String BASIC_AUTH_PREFIX = "Basic";
    String TOKEN_AUTH_PREFIX = "Bearer";

    String DEFAULT_REALM_KEY = "sf-ewf";

    String CHALLENGE_FORMAT = "%s realm=\"%s\"";

    String USERNAME_KEY = "ewf_uid_key";

    String JWT_UID_CLAIM_KEY = "uid";
    String JWT_ISSUER = "ewf";
}
