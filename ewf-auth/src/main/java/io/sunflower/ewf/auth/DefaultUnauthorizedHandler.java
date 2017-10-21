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

import io.sunflower.ewf.Result;
import io.sunflower.ewf.Results;

/**
 * DefaultUnauthorizedHandler
 *
 * @author michael created on 17/10/21 21:44
 */
public class DefaultUnauthorizedHandler implements UnauthorizedHandler {

  private static final String CHALLENGE_FORMAT = "%s realm=\"%s\"";

  @Override
  public Result onUnauthorized(String prefix, String realm) {
    return Results.status(Result.SC_401_UNAUTHORIZED)
        .addHeader(Result.WWW_AUTHENTICATE, String.format(CHALLENGE_FORMAT, prefix, realm))
        .contentType(Result.TEXT_PLAIN)
        .render("Credentials are required to access this resource.");
  }
}
