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

import javax.inject.Inject;

import com.google.common.base.Strings;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Filter;
import io.sunflower.ewf.FilterChain;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.spi.ExceptionHandler;

/**
 * SessionSecureFilter
 *
 * @author michael created on 17/10/27 15:58
 */
public class SessionSecureFilter implements Filter {

  /** If a username is saved we assume the session is valid */
  public static final String USERNAME = "username";

  @Inject
  private ExceptionHandler exceptionHandler;

  @Override
  public Result filter(FilterChain filterChain, Context context) {

    if (!Strings.isNullOrEmpty(context.getSession().get(USERNAME))) {
      return filterChain.next(context);
    }

    return exceptionHandler.getForbiddenResult(context);
  }
}
