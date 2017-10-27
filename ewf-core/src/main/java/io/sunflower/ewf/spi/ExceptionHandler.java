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

package io.sunflower.ewf.spi;

import com.google.inject.ImplementedBy;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.errors.BadRequestException;
import io.sunflower.ewf.spi.internal.DefaultExceptionHandler;

/**
 * @author michael
 */
@ImplementedBy(DefaultExceptionHandler.class)
public interface ExceptionHandler {

  /**
   * transform exception to response result.
   * @param context
   * @param exception
   * @return errorResult
   */
  Result onException(Context context, Exception exception);

  /**
   * Should handle cases where an exception is thrown when handling a route that let to an internal
   * server error.
   *
   * Should lead to a html error 500 - internal sever error (and be used with the same mindset).
   *
   * Usually used by onRouteRequest(...).
   * @param context
   * @param exception
   * @return
   */
  Result getInternalServerErrorResult(Context context, Exception exception);

  /**
   * Should handle cases where the client sent strange date that led to an error.
   *
   * Should lead to a html error 400 - bad request (and be used with the same mindset).
   *
   * Usually used by onRouteRequest(...).
   * @param context
   * @param exception
   * @return
   */
  Result getBadRequestResult(Context context, BadRequestException exception);

  /**
   * Should handle cases where no route can be found for a given request.
   *
   * Should lead to a html error 404 - not found (and be used with the same mindset).
   *
   * Usually used by handleRequest(...).
   * @param context
   * @return result
   */
  Result getNotFoundResult(Context context);

  /**
   * Should handle cases where access is unauthorized
   *
   * Should lead to a html error 401 - unauthorized
   * (and be used with the same mindset).
   *
   * By default, WWW-Authenticate is set to None.
   *
   * Usually used by BasicAuthFilter for instance(...).
   * @param context
   * @return
   */
  Result getUnauthorizedResult(Context context);

  /**
   * Should handle cases where access is forbidden
   *
   * Should lead to a html error 403 - forbidden (and be used with the same mindset).
   *
   * Usually used by SecureFilter for instance(...).
   * @param context
   * @return result
   */
  Result getForbiddenResult(Context context);

}
