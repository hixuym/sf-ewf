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

package io.sunflower.ewf.support;

/**
 * @author michael
 */
public interface Constants {

    /**
     * i18n keys and default messages of RequestHandler create the keys in your own messages.properties
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
     * A cookie that helps RequestHandler to set a default language. Usually resolves to a cookie called
     * EWF_LANG. The cookie then looks like: "EWF_LANG=en"
     */
    String LANG_COOKIE_SUFFIX = "_LANG";

    /**
     * Suffix used for RequestHandler cookies. Usually results in cookies like "EWF_SESSION
     */
    String SESSION_SUFFIX = "_SESSION";

    /**
     * Suffix used for RequestHandler cookies. Usually results in cookies like "EWF_FLASH
     */
    String FLASH_SUFFIX = "_FLASH";

    /**
     * Used as spacer for instance in session cookie
     */
    String UNI_CODE_NULL_ENTITY = "\u0000";

    String CONTROLLERS_DIR = "controllers";
    String VIEWS_DIR = "views";

    /**
     * settings configuration keys
     */

    String HANDLER_PATH_KEY = "ewf.handlerPath";
    String SF_UNDERTOW_CTX_PATH_KEY = "sf.undertowContextPath";
    String UPLOAD_FOLDER_KEY = "ewf.uploadTempFolder";
    String JSONP_CALLBACK_PARAM_KEY = "ewf.jsonpCallbackParam";
    String JAXY_ROUTES_ENABLED = "ewf.jaxyRoutesEnabled";
    String CONTROLLERS_SCAN_PKGS = "ewf.scanPkgs";
    String DIAGNOSTICS_ENABLED = "ewf.diagnosticsEnabled";
    String USAGE_OF_X_FORWARDED_HEADER_ENABLED = "ewf.usageOfXForwardedHeaderEnabled";
    String ETAG_ENABLE = "ewf.etagEnabled";
    String SESSION_SEND_ONLY_IF_CHANGED = "ewf.sessionSendOnlyIfChanged";
    String SESSION_TRANSFERRED_OVER_HTTPS_ONLY = "ewf.sessionTransferredOverHttpsOnly";
    String SESSION_HTTP_ONLY = "ewf.sessionHttpOnly";
    String COOKIE_ENCRYPTED = "ewf.cookieEncrypted";
    String COOKIE_PREFIX = "ewf.cookiePrefix";
    String COOKIE_DOMAIN = "ewf.cookieDomain";
    String SUPPORTED_LANGS = "ewf.supported_langs";
    String SECRET = "ewf.secret";
    String CACHE_MAX_AGE = "ewf.cacheMaxAge";
    String TOKEN_EXPIRE_TIME = "ewf.tokenExpireTime";
    String SESSION_EXPIRE_TIME = "ewf.sessionExpireTime";

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