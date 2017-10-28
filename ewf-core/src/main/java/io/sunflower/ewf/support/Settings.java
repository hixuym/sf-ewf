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

import com.google.common.base.Splitter;
import io.sunflower.util.Duration;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.sunflower.ewf.support.Constants.*;

/**
 * ewf framework settings
 *
 * @author michael
 */
@Singleton
public class Settings {

    private String applicationSecret = SecretGenerator.generateSecret();

    private List<String> applicationLangs = Arrays.asList("zh", "en");

    private String cookieDomain = "sunflower.io";
    private String cookiePrefix = "ewf_";
    private boolean cookieEncrypted = false;

    private Duration sessionExpireTime = Duration.hours(1);
    private boolean sessionSendOnlyIfChanged = true;
    private boolean sessionTransferredOverHttpsOnly = false;
    private boolean sessionHttpOnly = true;

    private Duration tokenExpireTime = Duration.days(7);

    private String httpCacheMaxAge = "3600";

    private boolean etagEnable = true;

    private Map<String, String> mimetypes = new HashMap<>();

    private String uploadTempFolder;
    private String jsonpCallbackParam = "jsonpCallback";
    private boolean diagnosticsEnabled = false;
    private boolean usageOfXForwardedHeaderEnabled = false;

    private Mode mode = ModeHelper.determineModeFromSystemPropertiesOrProdIfNotSet();

    private String handlerPath = "/";

    private String contextPath = "";

    private String scanPkgs = CONTROLLERS_DIR;

    private boolean jaxyRouteEnabled = true;

    public Settings() {
    }

    public Settings(Map<String, String> rawSettings) {
        loadSettings(rawSettings);
    }

    private void loadSettings(Map<String, String> rawSettings) {
        if (rawSettings.containsKey(HANDLER_PATH_KEY)) {
            this.handlerPath = rawSettings.get(HANDLER_PATH_KEY);
        }

        if (rawSettings.containsKey(SF_UNDERTOW_CTX_PATH_KEY)) {
            String contextPath =
                    StringUtils.removeEnd(rawSettings.get(SF_UNDERTOW_CTX_PATH_KEY), "/");

            String handlerPath = StringUtils.removeEnd(this.handlerPath, "/");

            this.contextPath = contextPath + handlerPath;
        }

        if (rawSettings.containsKey(MODE_KEY)) {
            this.mode = Mode.valueOf(rawSettings.get(MODE_KEY));
        }

        if (rawSettings.containsKey(UPLOAD_FOLDER_KEY)) {
            this.uploadTempFolder = rawSettings.get(UPLOAD_FOLDER_KEY);
        }

        if (rawSettings.containsKey(JSONP_CALLBACK_PARAM_KEY)) {
            this.jsonpCallbackParam = rawSettings.get(JSONP_CALLBACK_PARAM_KEY);
        }

        if (rawSettings.containsKey(CONTROLLERS_SCAN_PKGS)) {
            this.scanPkgs = rawSettings.get(CONTROLLERS_SCAN_PKGS);
        }

        if (rawSettings.containsKey(JAXY_ROUTES_ENABLED)) {
            this.jaxyRouteEnabled = Boolean.parseBoolean(rawSettings.get(JAXY_ROUTES_ENABLED));
        }

        if (rawSettings.containsKey(DIAGNOSTICS_ENABLED)) {
            this.diagnosticsEnabled = Boolean.parseBoolean(rawSettings.get(DIAGNOSTICS_ENABLED));
        }

        if (rawSettings.containsKey(Constants.USAGE_OF_X_FORWARDED_HEADER_ENABLED)) {
            this.usageOfXForwardedHeaderEnabled = Boolean
                    .parseBoolean(rawSettings.get(Constants.USAGE_OF_X_FORWARDED_HEADER_ENABLED));
        }

        if (rawSettings.containsKey(Constants.ETAG_ENABLE)) {
            this.etagEnable = Boolean.parseBoolean(rawSettings.get(Constants.ETAG_ENABLE));
        }

        if (rawSettings.containsKey(Constants.SESSION_SEND_ONLY_IF_CHANGED)) {
            this.sessionSendOnlyIfChanged = Boolean
                    .parseBoolean(rawSettings.get(Constants.SESSION_SEND_ONLY_IF_CHANGED));
        }

        if (rawSettings.containsKey(Constants.SESSION_TRANSFERRED_OVER_HTTPS_ONLY)) {
            this.sessionTransferredOverHttpsOnly = Boolean
                    .parseBoolean(rawSettings.get(Constants.SESSION_TRANSFERRED_OVER_HTTPS_ONLY));
        }

        if (rawSettings.containsKey(Constants.SESSION_HTTP_ONLY)) {
            this.sessionHttpOnly = Boolean.parseBoolean(rawSettings.get(Constants.SESSION_HTTP_ONLY));
        }

        if (rawSettings.containsKey(Constants.SESSION_HTTP_ONLY)) {
            this.cookieEncrypted = Boolean.parseBoolean(rawSettings.get(Constants.SESSION_HTTP_ONLY));
        }

        if (rawSettings.containsKey(Constants.COOKIE_PREFIX)) {
            this.cookiePrefix = rawSettings.get(Constants.COOKIE_PREFIX);
        }

        if (rawSettings.containsKey(Constants.COOKIE_DOMAIN)) {
            this.cookieDomain = rawSettings.get(Constants.COOKIE_DOMAIN);
        }

        if (rawSettings.containsKey(Constants.SUPPORTED_LANGS)) {
            this.applicationLangs = Splitter.on(",")
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(rawSettings.get(Constants.SUPPORTED_LANGS));
        }

        if (rawSettings.containsKey(Constants.SECRET)) {
            this.applicationSecret = rawSettings.get(Constants.SECRET);
        }

        if (rawSettings.containsKey(Constants.CACHE_MAX_AGE)) {
            this.httpCacheMaxAge = rawSettings.get(Constants.CACHE_MAX_AGE);
        }

        if (rawSettings.containsKey(Constants.TOKEN_EXPIRE_TIME)) {
            this.tokenExpireTime = Duration.parse(rawSettings.get(Constants.TOKEN_EXPIRE_TIME));
        }

        if (rawSettings.containsKey(Constants.SESSION_EXPIRE_TIME)) {
            this.sessionExpireTime = Duration.parse(rawSettings.get(Constants.SESSION_EXPIRE_TIME));
        }

        for (Map.Entry<String, String> e : rawSettings.entrySet()) {
            String key = e.getKey();
            String v = e.getValue();

            if (key.startsWith(PROPERTY_MIMETYPE_PREFIX)) {
                String type = key.substring(key.indexOf('.') + 1).toLowerCase();
                String value = rawSettings.get(key);
                mimetypes.put(type, value);
            }
        }

    }

    public String getContextPath() {
        return contextPath;
    }

    private static final String PROPERTY_MIMETYPE_PREFIX = "mimetype.";

    public String getHandlerPath() {
        return handlerPath;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public boolean isCookieEncrypted() {
        return cookieEncrypted;
    }

    public String getHttpCacheMaxAge() {
        return httpCacheMaxAge;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }

    public Duration getSessionExpireTime() {
        return sessionExpireTime;
    }

    public boolean isSessionSendOnlyIfChanged() {
        return sessionSendOnlyIfChanged;
    }

    public boolean isSessionTransferredOverHttpsOnly() {
        return sessionTransferredOverHttpsOnly;
    }

    public boolean isSessionHttpOnly() {
        return sessionHttpOnly;
    }

    public boolean isEtagEnable() {
        return etagEnable;
    }

    public Map<String, String> getMimetypes() {
        return mimetypes;
    }

    public String getUploadTempFolder() {
        return uploadTempFolder;
    }

    public String getJsonpCallbackParam() {
        return jsonpCallbackParam;
    }

    public boolean isDiagnosticsEnabled() {
        return diagnosticsEnabled;
    }

    public String getCookiePrefix() {
        return cookiePrefix;
    }

    public List<String> getApplicationLangs() {
        return applicationLangs;
    }

    public boolean isUsageOfXForwardedHeaderEnabled() {
        return usageOfXForwardedHeaderEnabled;
    }

    public boolean isProd() {
        return Mode.prod == this.mode;
    }

    public boolean isDev() {
        return this.mode == Mode.dev;
    }

    public boolean isTest() {
        return this.mode == Mode.test;
    }

    public Mode getMode() {
        return mode;
    }

    public Duration getTokenExpireTime() {
        return tokenExpireTime;
    }

    public String getControllerPkgs() {
        return this.scanPkgs;
    }

    public boolean isJaxyRouteEnabled() {
        return jaxyRouteEnabled;
    }
}
