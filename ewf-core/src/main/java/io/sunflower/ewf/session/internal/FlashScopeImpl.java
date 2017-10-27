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

package io.sunflower.ewf.session.internal;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Cookie;
import io.sunflower.ewf.session.FlashScope;
import io.sunflower.ewf.session.utils.CookieDataCodec;
import io.sunflower.ewf.support.Constants;
import io.sunflower.ewf.support.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default FlashScope implementation.
 * @author michael
 */
public class FlashScopeImpl implements FlashScope {

  static private Logger log = LoggerFactory.getLogger(io.sunflower.ewf.session.FlashScopeImpl.class);

  private final Map<String, String> currentFlashCookieData = new HashMap<>();
  private final Map<String, String> outgoingFlashCookieData = new HashMap<>();
  private final String applicationCookiePrefix;

  @Inject
  public FlashScopeImpl(Settings configuration) {
    this.applicationCookiePrefix = configuration.getCookiePrefix();
  }

  @Override
  public void init(Context context) {
    // get flash cookie:
    Cookie flashCookie = context.getCookie(applicationCookiePrefix + Constants.FLASH_SUFFIX);

    if (flashCookie != null) {
      try {

        CookieDataCodec.decode(currentFlashCookieData, flashCookie.getValue());

      } catch (UnsupportedEncodingException e) {
        log.error("Encoding exception - this must not happen", e);
      }
    }

  }

  @Override
  @SuppressWarnings("UseSpecificCatch")
  public void save(Context context) {

    if (outgoingFlashCookieData.isEmpty()) {
      // only need to buid a cookie (to empty its contents) if one currently exists
      if (context.hasCookie(applicationCookiePrefix
          + Constants.FLASH_SUFFIX)) {

        // build empty flash cookie
        Cookie.Builder cookie = Cookie.builder(applicationCookiePrefix
            + Constants.FLASH_SUFFIX, "");
        cookie.setPath(context.getContextPath() + "/");
        cookie.setSecure(false);
        cookie.setMaxAge(0);

        context.addCookie(cookie.build());
      }
    } else {
      // build a cookie with this flash data
      try {
        String flashData = CookieDataCodec.encode(outgoingFlashCookieData);

        Cookie.Builder cookie = Cookie.builder(applicationCookiePrefix
            + Constants.FLASH_SUFFIX, flashData);
        cookie.setPath(context.getContextPath() + "/");
        cookie.setSecure(false);
        // "-1" does not set "Expires" for that cookie
        // => Cookie will live as long as the browser is open theoretically
        cookie.setMaxAge(-1);

        context.addCookie(cookie.build());
      } catch (Exception e) {
        log.error("Encoding exception - this must not happen", e);
      }
    }
  }

  private void validateKey(String key) {
    if (key == null) {
      throw new IllegalArgumentException(
          "Flash key may not be null");
    }
    if (key.contains(":")) {
      throw new IllegalArgumentException(
          "Flash key may not contain character ':'");
    }
  }

  @Override
  public void now(String key, String value) {
    this.validateKey(key);
    currentFlashCookieData.put(key, value);
  }

  @Override
  public String get(String key) {
    this.validateKey(key);
    return currentFlashCookieData.get(key);
  }

  @Override
  public boolean remove(String key) {
    this.validateKey(key);
    this.outgoingFlashCookieData.remove(key);
    return currentFlashCookieData.remove(key) != null;
  }

  @Override
  public boolean contains(String key) {
    this.validateKey(key);
    return currentFlashCookieData.containsKey(key);
  }

  @Override
  public void put(String key, String value) {
    this.validateKey(key);
    currentFlashCookieData.put(key, value);
    outgoingFlashCookieData.put(key, value);
  }

  @Override
  public void error(String value) {
    put("error", value);
  }

  @Override
  public void success(String value) {
    put("success", value);
  }

  @Override
  public void discard(String key) {
    this.validateKey(key);
    outgoingFlashCookieData.remove(key);
  }

  @Override
  public void discard() {
    outgoingFlashCookieData.clear();
  }

  @Override
  public void keep(String key) {
    this.validateKey(key);
    if (currentFlashCookieData.containsKey(key)) {
      outgoingFlashCookieData.put(key, currentFlashCookieData.get(key));
    }
  }

  @Override
  public void keep() {
    outgoingFlashCookieData.putAll(currentFlashCookieData);
  }

  @Override
  public void clearCurrentFlashCookieData() {
    currentFlashCookieData.clear();
  }

  @Override
  public Map<String, String> getCurrentFlashCookieData() {
    return currentFlashCookieData;
  }

  @Override
  public Map<String, String> getOutgoingFlashCookieData() {
    return outgoingFlashCookieData;
  }

}