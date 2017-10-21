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

package io.sunflower.ewf;

import com.google.inject.ImplementedBy;

/**
 * @author michael
 */
@ImplementedBy(DefaultExceptionHandler.class)
public interface ExceptionHandler {

  /**
   * transform exception to response result.
   * @param context
   * @param exception
   * @return
   */
  Result onException(Exception exception, Context context);

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
