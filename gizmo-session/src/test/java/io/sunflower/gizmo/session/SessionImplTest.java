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

package io.sunflower.gizmo.session;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import io.sunflower.gizmo.Context;
import io.sunflower.gizmo.Cookie;
import io.sunflower.gizmo.GizmoConfiguration;
import io.sunflower.gizmo.Result;
import io.sunflower.gizmo.session.utils.Clock;
import io.sunflower.gizmo.session.utils.CookieEncryption;
import io.sunflower.gizmo.session.utils.Crypto;
import io.sunflower.gizmo.utils.GizmoConstant;
import io.sunflower.gizmo.utils.SecretGenerator;
import io.sunflower.util.Duration;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class SessionImplTest {

  @Mock
  private Context context;

  @Mock
  private Result result;

  @Captor
  private ArgumentCaptor<Cookie> cookieCaptor;

  private Crypto crypto;
  private CookieEncryption encryption;

  @Mock
  GizmoConfiguration configuration;

  @Mock
  Clock clock;

  @Parameter
  public boolean encrypted;

  /**
   * This method provides parameters for {@code encrypted} field. The first set contains {@code
   * false} so that {@link CookieEncryption} is not initialized and test class is run without
   * session cookie encryption. Second set contains {@code true} so that sessions cookies are
   * encrypted.
   */
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{{false}, {true}});
  }

  @Before
  public void setUp() {

    MockitoAnnotations.initMocks(this);

    when(
        configuration
            .getSessionExpireTime())
        .thenReturn(Duration.seconds(10000));
    when(
        configuration.isSessionSendOnlyIfChanged())
        .thenReturn(true);
    when(
        configuration.isSessionTransferredOverHttpsOnly())
        .thenReturn(true);
    when(
        configuration.isSessionHttpOnly()).thenReturn(true);

    when(configuration.getApplicationSecret())
        .thenReturn(SecretGenerator.generateSecret());

    when(configuration.getCookiePrefix())
        .thenReturn("NINJA");

    when(clock.currentTimeMillis())
        .thenReturn(System.currentTimeMillis());

    when(configuration.isCookieEncrypted()).thenReturn(encrypted);

    encryption = new CookieEncryption(configuration);
    crypto = new Crypto(configuration);

  }

  @Test
  public void testSessionDoesNotGetWrittenToResponseWhenEmptyAndOnlySentWhenChanged() {

    Session sessionCookie =
        createNewSession();

    sessionCookie.init(context);

    // put nothing => empty session will not be sent as we send only changed
    // stuff...
    sessionCookie.save(context);

    // no cookie should be set as the flash scope is empty...:
    verify(context, never()).addCookie(Matchers.any(Cookie.class));
  }

  @Test
  public void testSessionCookieSettingWorks() throws Exception {

    Session sessionCookie = createNewSession();

    sessionCookie.init(context);

    sessionCookie.put("hello", "session!");

    // put nothing => intentionally to check if no session cookie will be
    // saved
    sessionCookie.save(context);

    // a cookie will be set
    verify(context).addCookie(cookieCaptor.capture());

    // verify some stuff on the set cookie
    assertEquals("NINJA_SESSION", cookieCaptor.getValue().getName());

    // assert some stuff...
    // Make sure that sign is valid:
    String cookieString = cookieCaptor.getValue().getValue();

    String cookieFromSign = cookieString.substring(cookieString.indexOf("-") + 1);

    String computedSign = crypto.signHmacSha1(cookieFromSign);

    assertEquals(computedSign, cookieString.substring(0, cookieString.indexOf("-")));

    if (encrypted) {
      cookieFromSign = encryption.decrypt(cookieFromSign);
    }
    // Make sure that cookie contains timestamp
    assertTrue(cookieFromSign.contains(Session.TIMESTAMP_KEY));

  }

  @Test
  public void testHttpsOnlyWorks() throws Exception {

    Session sessionCookie = createNewSession();

    sessionCookie.init(context);

    sessionCookie.put("hello", "session!");

    // put nothing => intentionally to check if no session cookie will be
    // saved
    sessionCookie.save(context);

    // a cookie will be set
    verify(context).addCookie(cookieCaptor.capture());

    // verify some stuff on the set cookie
    assertEquals(true, cookieCaptor.getValue().isSecure());
  }

  @Test
  public void testNoHttpsOnlyWorks() throws Exception {
    // setup this testmethod
    when(
        configuration.isSessionTransferredOverHttpsOnly())
        .thenReturn(false);

    Session sessionCookie = createNewSession();

    sessionCookie.init(context);

    sessionCookie.put("hello", "session!");

    // put nothing => intentionally to check if no session cookie will be
    // saved
    sessionCookie.save(context);

    // a cookie will be set
    verify(context).addCookie(cookieCaptor.capture());

    // verify some stuff on the set cookie
    assertEquals(false, cookieCaptor.getValue().isSecure());

  }

  @Test
  public void testHttpOnlyWorks() throws Exception {

    Session sessionCookie = createNewSession();

    sessionCookie.init(context);

    sessionCookie.put("hello", "session!");

    // put nothing => intentionally to check if no session cookie will be
    // saved
    sessionCookie.save(context);

    // a cookie will be set
    verify(context).addCookie(cookieCaptor.capture());

    // verify some stuff on the set cookie
    assertEquals(true, cookieCaptor.getValue().isHttpOnly());

  }

  @Test
  public void testNoHttpOnlyWorks() throws Exception {
    // setup this testmethod
    when(
        configuration.isSessionHttpOnly()).thenReturn(false);

    Session sessionCookie = createNewSession();

    sessionCookie.init(context);

    sessionCookie.put("hello", "session!");

    // put nothing => intentionally to check if no session cookie will be
    // saved
    sessionCookie.save(context);

    // a cookie will be set
    verify(context).addCookie(cookieCaptor.capture());

    // verify some stuff on the set cookie
    assertEquals(false, cookieCaptor.getValue().isHttpOnly());

  }

  @Test
  public void testThatCookieSavingAndInitingWorks() {

    Session sessionCookie = createNewSession();

    sessionCookie.init(context);

    sessionCookie.put("key1", "value1");
    sessionCookie.put("key2", "value2");
    sessionCookie.put("key3", "value3");

    // put nothing => intentionally to check if no session cookie will be
    // saved
    sessionCookie.save(context);

    // a cookie will be set
    verify(context).addCookie(cookieCaptor.capture());

    // now we simulate a new request => the session storage will generate a
    // new cookie:
    Cookie newSessionCookie = Cookie.builder(
        cookieCaptor.getValue().getName(),
        cookieCaptor.getValue().getValue()).build();

    // that will be returned by the httprequest...
    when(context.getCookie(cookieCaptor.getValue().getName())).thenReturn(
        newSessionCookie);

    // init new session from that cookie:
    Session sessionCookie2 = createNewSession();

    sessionCookie2.init(context);

    assertEquals("value1", sessionCookie2.get("key1"));
    assertEquals("value2", sessionCookie2.get("key2"));
    assertEquals("value3", sessionCookie2.get("key3"));

  }

  @Test
  public void testThatCorrectMethodOfNinjaPropertiesIsUsedSoThatStuffBreaksWhenPropertyIsAbsent() {

    // we did not set the cookie prefix
    when(configuration.getCookiePrefix())
        .thenReturn(null);

    // stuff must break => ...
    Session sessionCookie = createNewSession();

    verify(configuration).getCookiePrefix();
  }

  @Test
  public void testSessionCookieDelete() {
    Session sessionCookie = createNewSession();
    sessionCookie.init(context);
    final String key = "mykey";
    final String value = "myvalue";
    sessionCookie.put(key, value);

    // value should have been set:
    assertEquals(value, sessionCookie.get(key));

    // value should be returned when removing:
    assertEquals(value, sessionCookie.remove(key));

    // after removing, value should not be there anymore:
    assertNull(sessionCookie.get(key));
  }

  @Test
  public void testGetAuthenticityTokenWorks() {

    Session sessionCookie = createNewSession();

    sessionCookie.init(context);

    String authenticityToken = sessionCookie.getAuthenticityToken();

    String cookieValueWithoutSign = captureFinalCookie(sessionCookie);

    //verify that the authenticity token is set
    assertTrue(cookieValueWithoutSign.contains(Session.AUTHENTICITY_KEY + "=" + authenticityToken));
    // also make sure the timestamp is there:
    assertTrue(cookieValueWithoutSign.contains(Session.TIMESTAMP_KEY));

  }

  @Test
  public void testGetIdTokenWorks() {

    Session sessionCookie = createNewSession();

    sessionCookie.init(context);

    String idToken = sessionCookie.getId();

    String valueWithoutSign = captureFinalCookie(sessionCookie);
    //verify that the id token is set:
    assertTrue(valueWithoutSign.contains(Session.ID_KEY + "=" + idToken));
    // also make sure the timestamp is there:
    assertTrue(valueWithoutSign.contains(Session.TIMESTAMP_KEY));

  }

  @Test
  public void testThatCookieUsesContextPath() {
    Mockito.when(context.getContextPath()).thenReturn("/my_context");
    Session sessionCookie = createNewSession();
    sessionCookie.init(context);
    sessionCookie.put("anykey", "anyvalue");

    sessionCookie.save(context);

    verify(context).addCookie(cookieCaptor.capture());
    Cookie cookie = cookieCaptor.getValue();
    assertThat(cookie.getPath(), CoreMatchers.equalTo("/my_context/"));
  }

  @Test
  public void testExpiryTime() {
    // 1. Check that session is still saved when expiry time is set

    Session sessionCookie1 = createNewSession();
    sessionCookie1.init(context);

    sessionCookie1.put("a", "2");

    sessionCookie1.setExpiryTime(10 * 1000L);

    assertThat(sessionCookie1.get("a"), CoreMatchers.equalTo("2"));

    sessionCookie1.save(context);

    Session sessionCookie2 = roundTrip(sessionCookie1);

    assertThat(sessionCookie2.get("a"), CoreMatchers.equalTo("2"));

    // 2. Check that session is invalidated when past the expiry time

    // Set the current time past when it is called.
    when(clock.currentTimeMillis()).thenReturn(System.currentTimeMillis() + 11 * 1000L);

    Session sessionCookie3 = roundTrip(sessionCookie2);

    assertNull(sessionCookie3.get("a"));
  }

  @Test
  public void testExpiryTimeRoundTrip() {
    // Round trip the session cookie with an expiry time in the future
    // Then remove the expiration time to make sure it is still valid
    when(
        configuration.getSessionExpireTime())
        .thenReturn(null);

    Session sessionCookie1 = createNewSession();
    sessionCookie1.init(context);

    sessionCookie1.put("a", "2");

    sessionCookie1.setExpiryTime(10 * 1000L);

    assertThat(sessionCookie1.get("a"), CoreMatchers.equalTo("2"));

    Session sessionCookie2 = roundTrip(sessionCookie1);

    assertThat(sessionCookie2.get("a"), CoreMatchers.equalTo("2"));

    assertThat(sessionCookie2.get(Session.EXPIRY_TIME_KEY), CoreMatchers.equalTo("10000"));

    sessionCookie2.setExpiryTime(null);

    Session sessionCookie3 = roundTrip(sessionCookie2);

    assertNull(sessionCookie3.get(Session.EXPIRY_TIME_KEY));
  }

  @Test
  public void testThatCookieDoesNotUseApplicationDomainWhenNotSet() {
    when(configuration.getCookieDomain()).thenReturn(null);
    Session sessionCookie = createNewSession();
    sessionCookie.init(context);
    sessionCookie.put("anykey", "anyvalue");

    sessionCookie.save(context);

    verify(context).addCookie(cookieCaptor.capture());
    Cookie cookie = cookieCaptor.getValue();
    assertThat(cookie.getDomain(), CoreMatchers.equalTo(null));
  }

  @Test
  public void testThatCookieUseApplicationDomain() {
    when(configuration.getCookieDomain()).thenReturn("domain.com");
    Session sessionCookie = createNewSession();
    sessionCookie.init(context);
    sessionCookie.put("anykey", "anyvalue");

    sessionCookie.save(context);

    verify(context).addCookie(cookieCaptor.capture());
    Cookie cookie = cookieCaptor.getValue();
    assertThat(cookie.getDomain(), CoreMatchers.equalTo("domain.com"));
  }

  @Test
  public void testThatCookieClearWorks() {
    String applicationCookieName = configuration.getCookiePrefix()
        + GizmoConstant.SESSION_SUFFIX;

    // First roundtrip
    Session sessionCookie = createNewSession();
    sessionCookie.init(context);
    sessionCookie.put("anykey", "anyvalue");

    Session sessionCookieWithValues = roundTrip(sessionCookie);

    // Second roundtrip with cleared session
    sessionCookieWithValues.clear();
    when(context.hasCookie(applicationCookieName)).thenReturn(true);

    // Third roundtrip
    String cookieValue = captureFinalCookie(sessionCookieWithValues);
    assertThat(cookieValue, not(containsString("anykey")));

    assertThat(cookieCaptor.getValue().getDomain(), CoreMatchers.equalTo(null));
    assertThat(cookieCaptor.getValue().getMaxAge(), CoreMatchers.equalTo(0));
  }

  @Test
  public void testThatCookieClearWorksWithApplicationDomain() {
    String applicationCookieName = configuration.getCookiePrefix()
        + GizmoConstant.SESSION_SUFFIX;
    when(configuration.getCookieDomain()).thenReturn("domain.com");

    // First roundtrip
    Session sessionCookie = createNewSession();
    sessionCookie.init(context);
    sessionCookie.put("anykey", "anyvalue");

    Session sessionCookieWithValues = roundTrip(sessionCookie);

    // Second roundtrip with cleared session
    sessionCookieWithValues.clear();
    when(context.hasCookie(applicationCookieName)).thenReturn(true);

    // Third roundtrip
    String cookieValue = captureFinalCookie(sessionCookieWithValues);
    assertThat(cookieValue, not(containsString("anykey")));

    assertThat(cookieCaptor.getValue().getDomain(), CoreMatchers.equalTo("domain.com"));
    assertThat(cookieCaptor.getValue().getMaxAge(), CoreMatchers.equalTo(0));
  }

  @Test
  public void testSessionEncryptionKeysMismatch() {

    if (!encrypted) {
      assertTrue("N/A for plain session cookies without encryption", true);
      return;
    }

    // (1) create session with some data and save
    Session session_1 = createNewSession();
    session_1.init(context);
    session_1.put("key", "value");
    session_1.save(context);

    // (2) verify that cookie with our data is created and added to context
    verify(context).addCookie(cookieCaptor.capture());
    assertEquals("value", session_1.get("key"));

    // save reference to our cookie - we will use it to init sessions below
    Cookie cookie = cookieCaptor.getValue();

    // (3) create new session with the same cookie and assert that it still has our data
    Session session_2 = createNewSession();
    when(context.getCookie("NINJA_SESSION")).thenReturn(cookie);
    session_2.init(context);
    assertFalse(session_2.isEmpty());
    assertEquals("value", session_2.get("key"));

    // (4) now we change our application secret and thus our encryption key is modified
    when(configuration.getApplicationSecret())
        .thenReturn(SecretGenerator.generateSecret());
    encryption = new CookieEncryption(configuration);

    // (5) creating new session with the same cookie above would result in clean session
    // because that cookie was encrypted with another key and decryption with the new key
    // is not possible; usually such a case throws `javax.crypto.BadPaddingException`
    Session session_3 = createNewSession();
    session_3.init(context);
    assertTrue(session_3.isEmpty());
  }

  private Session roundTrip(Session sessionCookie1) {
    sessionCookie1.save(context);

    // Get the cookie ...
    verify(context, atLeastOnce()).addCookie(cookieCaptor.capture());

    when(context.getCookie("NINJA_SESSION")).thenReturn(cookieCaptor.getValue());

    // ... and roundtrip it into an new session
    Session sessionCookie2 = createNewSession();
    sessionCookie2.init(context);
    return sessionCookie2;
  }

  private Session createNewSession() {
    return new SessionImpl(crypto, encryption, configuration, clock);
  }

  private String captureFinalCookie(Session sessionCookie) {
    sessionCookie.save(context);

    // SessionImpl should set the cookie
    verify(context, atLeastOnce()).addCookie(cookieCaptor.capture());

    String cookieValue = cookieCaptor.getValue().getValue();
    String cookieValueWithoutSign = cookieValue.substring(cookieValue.indexOf("-") + 1);

    if (encrypted) {
      cookieValueWithoutSign = encryption.decrypt(cookieValueWithoutSign);
    }

    return cookieValueWithoutSign;
  }
}