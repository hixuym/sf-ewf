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

package io.sunflower.ewf.assets;

import io.sunflower.ewf.Context;
import io.sunflower.ewf.Renderable;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.Results;
import io.sunflower.ewf.support.HttpCacheToolkit;
import io.sunflower.ewf.support.MimeTypes;
import io.sunflower.ewf.support.ResponseStreams;
import io.sunflower.ewf.support.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetsControllerTest {

    @Mock
    MimeTypes mimeTypes;

    @Mock
    HttpCacheToolkit httpCacheToolkit;

    @Mock
    Context contextRenderable;

    @Captor
    ArgumentCaptor<Result> resultCaptor;

    @Mock
    ResponseStreams responseStreams;

    @Mock
    Settings configuration;

    AssetsController assetsController;

    @Before
    public void before() {
        assetsController = new AssetsController(
                new AssetsControllerHelper(),
                httpCacheToolkit,
                mimeTypes,
                configuration);
    }


    @Test
    public void testServeStatic404() throws Exception {
        when(contextRenderable.getRequestPath()).thenReturn("notAvailable");
        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();

        Result result = Results.ok();

        renderable.render(contextRenderable, result);

        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());
        assertEquals(Results.notFound().getStatusCode(), resultCaptor.getValue().getStatusCode());

    }

    @Test
    public void testServeStaticSecurityClassesWithoutSlash() throws Exception {
        when(contextRenderable.getRequestPath()).thenReturn("ninja/Ninja.class");
        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();

        Result result = Results.ok();

        renderable.render(contextRenderable, result);

        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());
        assertEquals(Results.notFound().getStatusCode(), resultCaptor.getValue().getStatusCode());

    }

    @Test
    public void testServeStaticSecurityClassesAbsolute() throws Exception {

        when(contextRenderable.getRequestPath()).thenReturn("/ninja/Ninja.class");
        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();

        Result result = Results.ok();

        renderable.render(contextRenderable, result);

        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());
        assertEquals(Results.notFound().getStatusCode(), resultCaptor.getValue().getStatusCode());

    }

    @Test
    public void testServeStaticSecurityNoRelativPathWorks() throws Exception {
        //This theoretically could work as robots.txt is there..
        // But it should
        when(contextRenderable.getRequestPath()).thenReturn("/assets/../../conf/heroku.conf");

        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();

        Result result = Results.ok();

        renderable.render(contextRenderable, result);

        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());
        assertEquals(Results.notFound().getStatusCode(), resultCaptor.getValue().getStatusCode());
    }

    @Test
    public void testServeStaticDirectory() throws Exception {
        AssetsControllerHelper assetsControllerHelper = Mockito
                .mock(AssetsControllerHelper.class, Mockito.CALLS_REAL_METHODS);

        assetsController = new AssetsController(
                assetsControllerHelper,
                httpCacheToolkit,
                mimeTypes,
                configuration);

        when(contextRenderable.getRequestPath()).thenReturn("/");
        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();

        Result result = Results.ok();

        renderable.render(contextRenderable, result);

        verify(assetsControllerHelper).isDirectoryURL(this.getClass().getResource("/assets/"));
        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());
        assertEquals(Results.notFound().getStatusCode(), resultCaptor.getValue().getStatusCode());
    }

    @Test
    public void testServeStatic304NotModified() throws Exception {

        when(contextRenderable.getRequestPath()).thenReturn(
                "/assets/testasset.txt");

        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();

        Result result = Results.ok();
        // manually set to not modified => asset controller should
        // only finalize, but not stream
        result.status(Result.SC_304_NOT_MODIFIED);

        renderable.render(contextRenderable, result);
        // test streaming of resource:
        // => not modified:
        // check etag has been called
        verify(httpCacheToolkit).addEtag(Mockito.eq(contextRenderable),
                Mockito.eq(result), Mockito.anyLong());

        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());

        // make sure we get the correct result...
        assertEquals(Result.SC_304_NOT_MODIFIED, resultCaptor.getValue()
                .getStatusCode());

    }

    @Test
    public void testStaticDirectoryIsFileSystemInDevMode() throws Exception {

        // some more setup needed:
        Mockito.when(configuration.isDev()).thenReturn(true);
        AssetsControllerHelper assetsControllerHelper = Mockito
                .mock(AssetsControllerHelper.class, Mockito.CALLS_REAL_METHODS);

        assetsController = new AssetsController(
                assetsControllerHelper,
                httpCacheToolkit,
                mimeTypes,
                configuration);

        when(contextRenderable.getRequestPath()).thenReturn(
                "/assets/testasset-not-existent.txt");

        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();
        renderable.render(contextRenderable, Results.ok());
        verify(assetsControllerHelper)
                .normalizePathWithoutLeadingSlash("/assets/testasset-not-existent.txt", true);
        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());
        assertEquals(404, resultCaptor.getValue().getStatusCode());

    }

    @Test
    public void testStaticDirectoryClassPathWhenFileNotInFileSystemInDevMode() throws Exception {

        // some more setup needed:
        Mockito.when(configuration.isDev()).thenReturn(true);
        AssetsControllerHelper assetsControllerHelper = Mockito
                .mock(AssetsControllerHelper.class, Mockito.CALLS_REAL_METHODS);

        assetsController = new AssetsController(
                assetsControllerHelper,
                httpCacheToolkit,
                mimeTypes,
                configuration);

        when(contextRenderable.getRequestPath()).thenReturn(
                "/assets/testasset.txt");
        when(contextRenderable.finalizeHeadersWithoutFlashAndSessionCookie(Mockito.any(Result.class)))
                .thenReturn(
                        responseStreams);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(responseStreams.getOutputStream()).thenReturn(
                byteArrayOutputStream);

        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();
        renderable.render(contextRenderable, Results.ok());
        verify(assetsControllerHelper).normalizePathWithoutLeadingSlash("/assets/testasset.txt", true);
        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());
        assertEquals(200, resultCaptor.getValue().getStatusCode());

    }

    @Test
    public void testStaticDirectoryIsClassPathInProdMode() throws Exception {

        // some more setup needed:
        Mockito.when(configuration.isDev()).thenReturn(false);
        AssetsControllerHelper assetsControllerHelper = Mockito
                .mock(AssetsControllerHelper.class, Mockito.CALLS_REAL_METHODS);
        assetsController = new AssetsController(
                assetsControllerHelper,
                httpCacheToolkit,
                mimeTypes,
                configuration);
        when(contextRenderable.getRequestPath()).thenReturn(
                "/assets/testasset.txt");
        when(contextRenderable.finalizeHeadersWithoutFlashAndSessionCookie(Mockito.any(Result.class)))
                .thenReturn(
                        responseStreams);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(responseStreams.getOutputStream()).thenReturn(
                byteArrayOutputStream);

        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();
        renderable.render(contextRenderable, Results.ok());
        verify(assetsControllerHelper).normalizePathWithoutLeadingSlash("/assets/testasset.txt", true);

    }

    @Test
    public void testServeStaticNormalOperationModifiedNoCaching()
            throws Exception {

        Result result = Results.ok();

        when(contextRenderable.getRequestPath()).thenReturn(
                "/assets/testasset.txt");

        when(mimeTypes.getContentType(Mockito.eq(contextRenderable),
                Mockito.anyString())).thenReturn("mimetype");

        when(contextRenderable.finalizeHeadersWithoutFlashAndSessionCookie(Mockito.eq(result)))
                .thenReturn(
                        responseStreams);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(responseStreams.getOutputStream()).thenReturn(
                byteArrayOutputStream);
        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();

        renderable.render(contextRenderable, result);
        // test streaming of resource:
        // => not modified:
        // check etag has been called
        verify(httpCacheToolkit).addEtag(Mockito.eq(contextRenderable),
                Mockito.eq(result), Mockito.anyLong());

        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());

        // make sure we get the correct result...
        assertEquals(Result.SC_200_OK, resultCaptor.getValue().getStatusCode());
        // we mocked this one:
        assertEquals("mimetype", result.getContentType());

        // make sure the content is okay...
        assertEquals("testasset", byteArrayOutputStream.toString());

    }

    @Test
    public void testServeStaticRobotsTxt()
            throws Exception {

        Result result = Results.ok();

        when(contextRenderable.getRequestPath()).thenReturn(
                "/robots.txt");

        when(mimeTypes.getContentType(Mockito.eq(contextRenderable),
                Mockito.anyString())).thenReturn("mimetype");

        when(contextRenderable.finalizeHeadersWithoutFlashAndSessionCookie(Mockito.eq(result)))
                .thenReturn(
                        responseStreams);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(responseStreams.getOutputStream()).thenReturn(
                byteArrayOutputStream);
        Result result2 = assetsController.serveStatic();

        Renderable renderable = (Renderable) result2.getRenderable();

        renderable.render(contextRenderable, result);
        // test streaming of resource:
        // => not modified:
        // check etag has been called
        verify(httpCacheToolkit).addEtag(Mockito.eq(contextRenderable),
                Mockito.eq(result), Mockito.anyLong());

        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());

        // make sure we get the correct result...
        assertEquals(Result.SC_200_OK, resultCaptor.getValue().getStatusCode());
        // we mocked this one:
        assertEquals("mimetype", result.getContentType());

        String content = byteArrayOutputStream.toString();
        assertThat(content, containsString("User-agent: *"));
        assertThat(content, containsString("Disallow: /"));
    }

    @Test
    public void testServeWebJars() throws Exception {
        AssetsControllerHelper assetsControllerHelper
                = Mockito.mock(AssetsControllerHelper.class, Mockito.CALLS_REAL_METHODS);
        assetsController = new AssetsController(
                assetsControllerHelper,
                httpCacheToolkit,
                mimeTypes,
                configuration);
        Result result = Results.ok();

        when(contextRenderable.getRequestPath()).thenReturn(
                "/webjar_asset.txt");

        when(contextRenderable.finalizeHeadersWithoutFlashAndSessionCookie(Mockito.eq(result)))
                .thenReturn(
                        responseStreams);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        when(responseStreams.getOutputStream()).thenReturn(
                byteArrayOutputStream);
        Result result2 = assetsController.serveWebJars();

        Renderable renderable = (Renderable) result2.getRenderable();

        renderable.render(contextRenderable, result);

        verify(contextRenderable).finalizeHeadersWithoutFlashAndSessionCookie(resultCaptor.capture());

        // make sure we get the correct result...
        assertEquals(Result.SC_200_OK, resultCaptor.getValue().getStatusCode());

        assertEquals("webjar_asset", byteArrayOutputStream.toString());
        verify(assetsControllerHelper).normalizePathWithoutLeadingSlash("/webjar_asset.txt", true);

    }
}