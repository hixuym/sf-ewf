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

package io.sunflower.ewf.auth.params;

import java.security.Principal;

import io.sunflower.ewf.Context;
import io.sunflower.ewf.SecurityContext;
import io.sunflower.ewf.params.ArgumentExtractor;

/**
 * PrincipalExtractor
 *
 * @author michael created on 17/10/23 15:59
 */
public class PrincipalExtractor implements ArgumentExtractor<Principal> {

  private final Auth auth;

  public PrincipalExtractor(Auth auth) {
    this.auth = auth;
  }

  @Override
  public Principal extract(Context context) {
    SecurityContext securityContext = context.getSecurityContext();
    return securityContext != null ? securityContext.getUserPrincipal() : null;
  }

  @Override
  public Class<Principal> getExtractedType() {
    return Principal.class;
  }

  @Override
  public String getFieldName() {
    return auth.value();
  }
}
