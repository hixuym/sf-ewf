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

package io.sunflower.ewf.utils;

/**
 * @author michael
 */
public interface Constants {

  /**
   * i18n keys and default messages of RouteHandler create the keys in your own messages.properties
   * file to customize the message
   */
  String I18N_SYSTEM_BAD_REQUEST_TEXT_KEY = "ewf.system.bad_request.text";
  String I18N_SYSTEM_BAD_REQUEST_TEXT_DEFAULT = "Oops. That''s a bad request and all we know.";

  String I18N_SYSTEM_INTERNAL_SERVER_ERROR_TEXT_KEY = "ewf.system.internal_server_error.text";
  String I18N_SYSTEM_INTERNAL_SERVER_ERROR_TEXT_DEFAULT = "Oops. That''s an internal server error and all we know.";

  String I18N_SYSTEM_NOT_FOUND_TEXT_KEY = "ewf.system.not_found.text";
  String I18N_SYSTEM_NOT_FOUND_TEXT_DEFAULT = "Oops. The requested route cannot be found.";

  String I18N_SYSTEM_UNAUTHORIZED_REQUEST_TEXT_KEY = "ewf.system.unauthorized.text";
  String I18N_SYSTEM_UNAUTHORIZED_REQUEST_TEXT_DEFAULT = "Oops. You are unauthorized.";

  String I18N_SYSTEM_FORBIDDEN_REQUEST_TEXT_KEY = "ewf.system.forbidden.text";
  String I18N_SYSTEM_FORBIDDEN_REQUEST_TEXT_DEFAULT = "Oops. That''s forbidden and all we know.";

  /**
   * A cookie that helps RouteHandler to set a default language. Usually resolves to a cookie called
   * EWF_LANG. The cookie then looks like: "EWF_LANG=en"
   */
  String LANG_COOKIE_SUFFIX = "_LANG";

  /**
   * Suffix used for RouteHandler cookies. Usually results in cookies like "EWF_SESSION
   */
  String SESSION_SUFFIX = "_SESSION";

  /**
   * Suffix used for RouteHandler cookies. Usually results in cookies like "EWF_FLASH
   */
  String FLASH_SUFFIX = "_FLASH";

  /**
   * Used as spacer for instance in session cookie
   */
  String UNI_CODE_NULL_ENTITY = "\u0000";

  /**
   * yea. utf-8
   */
  String UTF_8 = "utf-8";

  String DATE_KEY = "validation.is.date.violation";
  String DATE_MESSAGE = "{0} must be a valid date";

  String ENUM_KEY = "validation.is.enum.violation";
  String ENUM_MESSAGE = "{0} is not a valid enum constant";

  String FLOAT_KEY = "validation.is.float.violation";
  String FLOAT_MESSAGE = "{0} must be a decimal number";

  String INT_KEY = "validation.is.integer.violation";
  String INT_MESSAGE = "{0} must be an integer";

  String SIZE_KEY = "validation.is.size.violation";
  String SIZE_MESSAGE = "{0} must be an size.";

  String DURATION_KEY = "validation.is.duration.violation";
  String DURATION_MESSAGE = "{0} must be an valid duration.";

  String AUTHENTICITY_TOKEN = "authenticityToken";
}