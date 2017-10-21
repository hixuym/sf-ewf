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

import com.google.inject.ImplementedBy;
import io.sunflower.ewf.Result;

/**
 * @author michael
 */
@ImplementedBy(DefaultUnauthorizedHandler.class)
public interface UnauthorizedHandler {

  /**
   * build the result when unauthorized
   * @param prefix
   * @param realm
   * @return result
   */
  Result onUnauthorized(String prefix, String realm);
}
