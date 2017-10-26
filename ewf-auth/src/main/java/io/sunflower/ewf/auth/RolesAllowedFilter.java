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

import java.lang.reflect.Method;
import javax.inject.Inject;

import io.sunflower.ewf.Context;
import io.sunflower.ewf.ExceptionHandler;
import io.sunflower.ewf.Filter;
import io.sunflower.ewf.FilterChain;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.Route;
import io.sunflower.ewf.SecurityContext;

/**
 * RolesAllowedFilter
 *
 * @author michael created on 17/10/21 22:19
 */
public class RolesAllowedFilter implements Filter {

  private final ExceptionHandler exceptionHandler;

  @Inject
  public RolesAllowedFilter(ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  public Result filter(FilterChain filterChain, Context context) {

    SecurityContext securityContext = context.getSecurityContext();

    // unauth
    if (securityContext == null) {
      return exceptionHandler.getUnauthorizedResult(context);
    }

    Route route = context.getRoute();

    Method method = route.getResourceMethod();
    Class<?> resourceClass = method.getDeclaringClass();

    if (method.isAnnotationPresent(PermitAll.class)) {
      return filterChain.next(context);
    }

    if (method.isAnnotationPresent(RolesAllowed.class)) {
      RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);

      if (securityContext.isUserInRole(rolesAllowed.value())) {
        return filterChain.next(context);
      }
    }

    if (resourceClass.isAnnotationPresent(PermitAll.class)) {
      return filterChain.next(context);
    }

    if (resourceClass.isAnnotationPresent(RolesAllowed.class)) {
      RolesAllowed rolesAllowed = resourceClass.getAnnotation(RolesAllowed.class);

      if (securityContext.isUserInRole(rolesAllowed.value())) {
        return filterChain.next(context);
      }
    }

    return exceptionHandler.getForbiddenResult(context);
  }
}
