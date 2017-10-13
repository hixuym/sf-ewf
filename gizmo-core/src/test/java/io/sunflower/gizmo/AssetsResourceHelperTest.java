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
package io.sunflower.gizmo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AssetsResourceHelperTest {

  AssetsResourceHelper assetsResourceHelper;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Before
  public void setup() {
    assetsResourceHelper = new AssetsResourceHelper();
  }

  @Test
  public void testNormalizePathWithoutLeadingSlash() {
    assertEquals("dir1/test.test",
        assetsResourceHelper.normalizePathWithoutLeadingSlash("/dir1/test.test", true));
    assertEquals("dir1/test.test",
        assetsResourceHelper.normalizePathWithoutLeadingSlash("dir1/test.test", true));
    //TODO
//        assertEquals(null, assetsResourceHelper.normalizePathWithoutLeadingSlash("/../test.test", true));
//        assertEquals(null, assetsResourceHelper.normalizePathWithoutLeadingSlash("../test.test", true));
    assertEquals("dir2/file.test",
        assetsResourceHelper.normalizePathWithoutLeadingSlash("/dir1/../dir2/file.test", true));
    assertEquals(null, assetsResourceHelper.normalizePathWithoutLeadingSlash(null, true));
    assertEquals("", assetsResourceHelper.normalizePathWithoutLeadingSlash("", true));
  }

  @Test
  public void testIsDirectoryURLWithJarProtocol() throws Exception {
    boolean result = assetsResourceHelper
        .isDirectoryURL(new URL("jar:file:/home/ninja/ninja.jar!/"));
    assertThat(result, is(false));
  }

  @Test
  public void testIsDirectoryURLWithFile() throws Exception {
    boolean result = assetsResourceHelper
        .isDirectoryURL(this.getClass().getResource("/assets/testasset.txt"));
    assertThat(result, is(false));
  }

  @Test
  public void testIsDirectoryURLWithDirectory() throws Exception {
    boolean result = assetsResourceHelper
        .isDirectoryURL(this.getClass().getResource("/assets/assets/"));
    assertThat(result, is(true));
  }

  @Test
  public void testIsDirectoryURLWithDirectoryContainsSpecialCharacters() throws Exception {
    File dir = tempFolder.newFolder("a#b");
    boolean result = assetsResourceHelper.isDirectoryURL(dir.toURI().toURL());
    assertThat(result, is(true));
  }

}
