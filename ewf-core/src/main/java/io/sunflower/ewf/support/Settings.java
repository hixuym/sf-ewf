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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;

import com.google.common.base.Splitter;
import io.sunflower.ewf.support.Mode;
import io.sunflower.ewf.support.ModeHelper;
import io.sunflower.ewf.support.SecretGenerator;
import io.sunflower.util.Duration;
import org.apache.commons.lang3.StringUtils;

/**
 * ewf framework settings
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

  public Settings() {
  }

  public Settings(Map<String, String> rawSettings) {
    loadSettings(rawSettings);
  }

  private static final String HANDLER_PATH_KEY = "ewf.handlerPath";
  private static final String SF_UNDERTOW_CTX_PATH_KEY = "sf.undertowContextPath";
  private static final String MODE_KEY = "ewf.mode";
  private static final String UPLOAD_FOLDER_KEY = "ewf.uploadTempFolder";
  private static final String JSONP_CALLBACK_PARAM_KEY = "ewf.jsonpCallbackParam";

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

    if (rawSettings.containsKey("ewf.diagnosticsEnabled")) {
      this.diagnosticsEnabled = Boolean.parseBoolean(rawSettings.get("ewf.diagnosticsEnabled"));
    }

    if (rawSettings.containsKey("ewf.usageOfXForwardedHeaderEnabled")) {
      this.usageOfXForwardedHeaderEnabled = Boolean
          .parseBoolean(rawSettings.get("ewf.usageOfXForwardedHeaderEnabled"));
    }

    if (rawSettings.containsKey("ewf.etagEnable")) {
      this.etagEnable = Boolean.parseBoolean(rawSettings.get("ewf.etagEnable"));
    }

    if (rawSettings.containsKey("ewf.sessionSendOnlyIfChanged")) {
      this.sessionSendOnlyIfChanged = Boolean
          .parseBoolean(rawSettings.get("ewf.sessionSendOnlyIfChanged"));
    }

    if (rawSettings.containsKey("ewf.sessionTransferredOverHttpsOnly")) {
      this.sessionTransferredOverHttpsOnly = Boolean
          .parseBoolean(rawSettings.get("ewf.sessionTransferredOverHttpsOnly"));
    }

    if (rawSettings.containsKey("ewf.sessionHttpOnly")) {
      this.sessionHttpOnly = Boolean.parseBoolean(rawSettings.get("ewf.sessionHttpOnly"));
    }

    if (rawSettings.containsKey("ewf.cookieEncrypted")) {
      this.cookieEncrypted = Boolean.parseBoolean(rawSettings.get("ewf.cookieEncrypted"));
    }

    if (rawSettings.containsKey("ewf.cookiePrefix")) {
      this.cookiePrefix = rawSettings.get("ewf.cookiePrefix");
    }

    if (rawSettings.containsKey("ewf.cookieDomain")) {
      this.cookieDomain = rawSettings.get("ewf.cookieDomain");
    }

    if (rawSettings.containsKey("ewf.supportedLangs")) {
      this.applicationLangs = Splitter.on(",")
          .omitEmptyStrings()
          .trimResults()
          .splitToList(rawSettings.get("ewf.supportedLangs"));
    }

    if (rawSettings.containsKey("ewf.secret")) {
      this.applicationSecret = rawSettings.get("ewf.secret");
    }

    if (rawSettings.containsKey("ewf.cacheMaxAge")) {
      this.httpCacheMaxAge = rawSettings.get("ewf.cacheMaxAge");
    }

    if (rawSettings.containsKey("ewf.tokenExpireTime")) {
      this.tokenExpireTime = Duration.parse(rawSettings.get("ewf.tokenExpireTime"));
    }

    if (rawSettings.containsKey("ewf.sessionExpireTime")) {
      this.sessionExpireTime = Duration.parse(rawSettings.get("ewf.sessionExpireTime"));
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
}
