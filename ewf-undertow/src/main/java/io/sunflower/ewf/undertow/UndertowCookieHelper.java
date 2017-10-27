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

package io.sunflower.ewf.undertow;

import javax.validation.constraints.NotNull;

import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michael
 */
public class UndertowCookieHelper {

  private static final Logger log = LoggerFactory.getLogger(UndertowCookieHelper.class);

  static public Cookie convertNinjaCookieToUndertowCookie(io.sunflower.ewf.Cookie gizmoCookie) {
    Cookie undertowCookie = new CookieImpl(gizmoCookie.getName(), gizmoCookie.getValue());

    undertowCookie.setMaxAge(gizmoCookie.getMaxAge());

    if (gizmoCookie.getComment() != null) {
      undertowCookie.setComment(gizmoCookie.getComment());
    }

    if (gizmoCookie.getDomain() != null) {
      undertowCookie.setDomain(gizmoCookie.getDomain());
    }

    if (gizmoCookie.getPath() != null) {
      undertowCookie.setPath(gizmoCookie.getPath());
    }

    undertowCookie.setSecure(gizmoCookie.isSecure());
    undertowCookie.setHttpOnly(gizmoCookie.isHttpOnly());

    // TODO: discard, version, and expires???

    return undertowCookie;
  }

  static public io.sunflower.ewf.Cookie convertUndertowCookieToNinjaCookie(
      @NotNull Cookie undertowCookie) {
    io.sunflower.ewf.Cookie.Builder gizmoCookieBuilder
        = io.sunflower.ewf.Cookie.builder(undertowCookie.getName(), undertowCookie.getValue());

    if (undertowCookie.getMaxAge() != null) {
      gizmoCookieBuilder.setMaxAge(undertowCookie.getMaxAge());
    }

    if (undertowCookie.getComment() != null) {
      gizmoCookieBuilder.setComment(undertowCookie.getComment());
    }

    if (undertowCookie.getDomain() != null) {
      gizmoCookieBuilder.setDomain(undertowCookie.getDomain());
    }

    if (undertowCookie.getPath() != null) {
      gizmoCookieBuilder.setPath(undertowCookie.getPath());
    }

    gizmoCookieBuilder.setHttpOnly(undertowCookie.isHttpOnly());
    gizmoCookieBuilder.setSecure(undertowCookie.isSecure());

    // TODO: discard, version, and expires???

    return gizmoCookieBuilder.build();
  }

}
