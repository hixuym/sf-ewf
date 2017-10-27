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
 * UserManager
 *
 * @author michael created on 17/10/27 14:31
 */
public interface UserManager<T> {

  String REQUEST_UID_KEY = "ewf_request_uid";

  /**
   * find user by key
   * @param uid
   * @return
   */
  T getUser(String uid);

  /**
   * verify user credentials
   * @param uid
   * @param password
   * @return
   */
  boolean verify(String uid, String password);
}
