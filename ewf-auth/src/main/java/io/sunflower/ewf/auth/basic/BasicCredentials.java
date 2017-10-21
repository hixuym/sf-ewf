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

package io.sunflower.ewf.auth.basic;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * A set of user-provided Basic Authentication credentials, consisting of a username and a
 * password.
 *
 * @author michael
 */
public class BasicCredentials {

  private final String username;
  private final String password;

  /**
   * Creates a new BasicCredentials with the given username and password.
   *
   * @param username the username
   * @param password the password
   */
  public BasicCredentials(String username, String password) {
    this.username = requireNonNull(username);
    this.password = requireNonNull(password);
  }

  /**
   * Returns the credentials' username.
   *
   * @return the credentials' username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Returns the credentials' password.
   *
   * @return the credentials' password
   */
  public String getPassword() {
    return password;
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final BasicCredentials other = (BasicCredentials) obj;
    return Objects.equals(this.username, other.username) && Objects
        .equals(this.password, other.password);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .add("password", "**********")
        .toString();
  }
}
