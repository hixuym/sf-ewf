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

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import com.auth0.jwt.interfaces.Claim;
import io.sunflower.ewf.support.Settings;
import io.sunflower.ewf.auth.token.JwtTokenHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * JwtTokenHelperTests
 *
 * @author michael created on 17/10/26 17:15
 */
public class JwtTokenHelperTests {

  @Test
  public void testJwtToken() {

    JwtTokenHelper helper = new JwtTokenHelper(new Settings());

    String token = helper.buildToken("michael", new Date(Instant.now().toEpochMilli() + 24*60*1000));

    Map<String, Claim> claims = helper.verify(token);

    Assert.assertNotNull(claims.get("uid"));

    Assert.assertEquals("michael", claims.get("uid").asString());
  }

}
