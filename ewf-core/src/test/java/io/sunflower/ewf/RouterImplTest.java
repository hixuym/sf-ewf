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

package io.sunflower.ewf;

import com.google.inject.Injector;
import com.google.inject.Provider;
import io.sunflower.ewf.internal.Route;
import io.sunflower.ewf.internal.RouteBuilderImpl;
import io.sunflower.ewf.internal.RouterImpl;
import io.sunflower.ewf.internal.ValidationImpl;
import io.sunflower.ewf.params.Param;
import io.sunflower.ewf.params.internal.ParamParsers;
import io.sunflower.ewf.support.ControllerMethods;
import io.sunflower.ewf.support.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * => Most tests are done via class RoutesTest in project ninja-servlet-integration-test.
 */
@RunWith(MockitoJUnitRunner.class)
public class RouterImplTest {

    Router router;

    @Mock
    Settings configuration;

    @Mock
    Injector injector;

    @Mock
    Provider<TestResource> testResourceProvider;

    ArgumentCaptor<Route> webSocketsCompileRouteCaptor;

    @Before
    @SuppressWarnings("Convert2Lambda")
    public void before() {
        webSocketsCompileRouteCaptor = ArgumentCaptor.forClass(Route.class);
        when(testResourceProvider.get()).thenReturn(new TestResource());
        when(injector.getProvider(TestResource.class)).thenReturn(testResourceProvider);
        when(injector.getInstance(ParamParsers.class))
                .thenReturn(new ParamParsers(Collections.emptySet()));
        Provider<RouteBuilderImpl> routeBuilderImplProvider = mock(Provider.class);
        when(routeBuilderImplProvider.get()).thenAnswer(
                (invocation) -> new RouteBuilderImpl());
        router = new RouterImpl(injector, routeBuilderImplProvider);

        // add route:
        router.GET().route("/testroute").with(TestResource.class, "index");
        router.GET().route("/user/{email}/{id: .*}").with(TestResource.class, "user");
        router.GET().route("/u{userId: .*}/entries/{entryId: .*}").with(TestResource.class, "entry");

        // second route to index should not break reverse routing matching the first
        router.GET().route("/testroute/another_url_by_index").with(TestResource.class, "index");
        router.GET().route("/ref").with(TestResource.class, "ref");

        // functional interface / lambda routing
        TestResource testResource1 = new TestResource("Hi!");
        router.GET().route("/any_instance_method_ref").with(TestResource::home);
        router.GET().route("/any_instance_method_ref_exception").with(TestResource::exception);
        router.GET().route("/any_instance_method_ref2").with(ControllerMethods.of(TestResource::home));
        router.GET().route("/specific_instance_method_ref").with(testResource1::message);
        router.GET().route("/specific_instance_method_ref_annotations").with(testResource1::status);
        router.GET().route("/anonymous_method_ref").with(() -> Results.status(202));
        Result staticResult = Results.status(208);
        router.GET().route("/anonymous_method_ref_captured").with(() -> staticResult);
        router.GET().route("/anonymous_method_ref_context")
                .with((Context context) -> Results.status(context.getParameterAsInteger("status")));
        router.GET().route("/anonymous_class").with(new ControllerMethods.ControllerMethod0() {
            @Override
            public Result apply() {
                return Results.status(203);
            }
        });
        router.GET().route("/anonymous_class_annotations")
                .with(new ControllerMethods.ControllerMethod1<Integer>() {
                    @Override
                    public Result apply(@Param("status") Integer status) {
                        return Results.status(status);
                    }
                });

        router.compileRoutes();
    }

    @Test
    public void getPathParametersEncodedWithNoPathParams() {
        Route route = router.getRouteFor("GET", "/testroute");

        Map<String, String> pathParameters = route.getPathParametersEncoded("/testroute");

        assertThat(pathParameters, aMapWithSize(0));
    }

    @Test
    public void routeForAnyInstanceMethodReference() {
        Route route = router.getRouteFor("GET", "/any_instance_method_ref");

        Result result = route.getFilterChain().next(null);

        assertThat(result.getStatusCode(), is(201));
    }

    @Test
    public void routeForAnyInstanceMethodReferenceThrowsException() {
        Route route = router.getRouteFor("GET", "/any_instance_method_ref_exception");

        try {
            Result result = route.getFilterChain().next(null);
            fail();
        } catch (Exception e) {
            assertThat(e.getCause().getMessage(), is("test"));
        }
    }

    @Test
    public void routeForAnyInstanceMethodReference2() {
        Route route = router.getRouteFor("GET", "/any_instance_method_ref2");

        Result result = route.getFilterChain().next(null);

        assertThat(result.getStatusCode(), is(201));
    }

    @Test
    public void routeForSpecificInstanceMethodReference() {
        Route route = router.getRouteFor("GET", "/specific_instance_method_ref");

        Result result = route.getFilterChain().next(null);

        // message set on specific instance
        assertThat(result.getRenderable(), is("Hi!"));
    }

    @Test
    public void routeForSpecificInstanceMethodReferenceWithAnnotations() {
        Context context = mock(Context.class);
        when(context.getParameter("status")).thenReturn("207");
        when(context.getValidation()).thenReturn(new ValidationImpl());

        Route route = router.getRouteFor("GET", "/specific_instance_method_ref_annotations");

        Result result = route.getFilterChain().next(context);

        // message set on specific instance
        assertThat(result.getStatusCode(), is(207));
        assertThat(result.getRenderable(), is("Hi!"));
    }

    @Test
    public void routeForAnonymoumsMethodReference() {
        Route route = router.getRouteFor("GET", "/anonymous_method_ref");

        Result result = route.getFilterChain().next(null);

        assertThat(result.getStatusCode(), is(202));
    }

    @Test
    public void routeForAnonymoumsMethodReferenceWithCaptured() {
        Context context = mock(Context.class);

        Route route = router.getRouteFor("GET", "/anonymous_method_ref_captured");

        Result result = route.getFilterChain().next(context);

        assertThat(result.getStatusCode(), is(208));
    }

    @Test
    public void routeForAnonymoumsMethodReferenceWithContext() {
        Context context = mock(Context.class);
        when(context.getParameterAsInteger("status")).thenReturn(206);

        Route route = router.getRouteFor("GET", "/anonymous_method_ref_context");

        Result result = route.getFilterChain().next(context);

        assertThat(result.getStatusCode(), is(206));
    }

    @Test
    public void routeForAnonymoumsClassInstance() {
        Route route = router.getRouteFor("GET", "/anonymous_class");

        Result result = route.getFilterChain().next(null);

        assertThat(result.getStatusCode(), is(203));
    }

    @Test
    public void routeForAnonymoumsClassInstanceWithAnnotations() {
        Context context = mock(Context.class);
        when(context.getParameter("status")).thenReturn("205");
        when(context.getValidation()).thenReturn(new ValidationImpl());

        Route route = router.getRouteFor("GET", "/anonymous_class_annotations");

        Result result = route.getFilterChain().next(context);

        assertThat(result.getStatusCode(), is(205));
    }

    /**
     * A dummy TestResource for mocking.
     */
    public static class TestResource {

        private final String message;

        public TestResource() {
            this("not set");
        }

        public TestResource(String message) {
            this.message = message;
        }

        public Result index() {
            return Results.ok();
        }

        public Result user() {
            return Results.ok();
        }

        public Result entry() {
            return Results.ok();
        }

        public Result ref() {
            return Results.ok();
        }

        public Result home() {
            return Results.status(201);
        }

        public Result message() {
            return Results.ok().render(message);
        }

        public Result status(@Param("status") Integer status) {
            return Results.status(status).render(message);
        }

        public Result exception() throws Exception {
            throw new Exception("test");
        }

//        public Result websocket() {
//            return Results.webSocketContinue();
//        }
    }

}
