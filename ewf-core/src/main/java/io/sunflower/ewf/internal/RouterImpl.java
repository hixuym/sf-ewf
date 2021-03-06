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

package io.sunflower.ewf.internal;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import io.sunflower.ewf.Router;
import io.sunflower.ewf.support.MethodReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author michael
 */
public class RouterImpl implements InternalRouter {

    static private final Logger logger = LoggerFactory.getLogger(RouterImpl.class);

    private final List<RouteBuilderImpl> allRouteBuilders = new ArrayList<>();
    private final Injector injector;
    private List<Route> routes;
    /**
     * for fast reverse route lookups
     */
    private Map<MethodReference, Route> reverseRoutes;
    /**
     * This regex works for both {myParam} AND {myParam: .*} (with regex)
     */
    private final String VARIABLE_PART_PATTERN_WITH_PLACEHOLDER = "\\{(%s)(:\\s([^}]*))?\\}";
    private final Provider<RouteBuilderImpl> routeBuilderImplProvider;

    @Inject
    public RouterImpl(Injector injector,
                      Provider<RouteBuilderImpl> routeBuilderImplProvider) {
        this.injector = injector;
        this.routeBuilderImplProvider = routeBuilderImplProvider;
    }

    @Override
    public Route getRouteFor(String httpMethod, String uri) {
        if (routes == null) {
            throw new IllegalStateException("Attempt to get route when routes not compiled");
        }

        for (Route route : routes) {
            if (route.matches(httpMethod, uri)) {
                return route;
            }
        }

        return null;

    }

    @Override
    public void compileRoutes() {
        if (routes != null) {
            throw new IllegalStateException("Routes already compiled");
        }

        List<Route> routesLocal = new ArrayList<>();

        for (RouteBuilderImpl routeBuilder : allRouteBuilders) {
            routesLocal.add(routeBuilder.buildRoute(injector));
        }

        this.routes = ImmutableList.copyOf(routesLocal);

        // compile reverse routes for O(1) lookups
        this.reverseRoutes = new HashMap<>(this.routes.size());

        for (Route route : this.routes) {
            // its possible this route is a Result instead of a resource method
            if (route.getControllerClass() != null) {
                MethodReference controllerMethodRef
                        = new MethodReference(
                        route.getControllerClass(),
                        route.getControllerMethod().getName());

                // the first one wins with reverse routing so we ignore when contains
                if (!this.reverseRoutes.containsKey(controllerMethodRef)) {
                    this.reverseRoutes.put(controllerMethodRef, route);
                }
            }
        }

        logRoutes();
    }

    @Override
    public List<Route> getRoutes() {
        verifyRoutesCompiled();
        return routes;
    }

    @Override
    public RouteBuilder GET() {

        RouteBuilderImpl routeBuilder = routeBuilderImplProvider.get().GET();
        allRouteBuilders.add(routeBuilder);

        return routeBuilder;
    }

    @Override
    public RouteBuilder WS() {

        RouteBuilderImpl routeBuilder = routeBuilderImplProvider.get().WS();
        allRouteBuilders.add(routeBuilder);

        return routeBuilder;
    }

    @Override
    public RouteBuilder POST() {
        RouteBuilderImpl routeBuilder = routeBuilderImplProvider.get().POST();
        allRouteBuilders.add(routeBuilder);

        return routeBuilder;
    }

    @Override
    public RouteBuilder PUT() {
        RouteBuilderImpl routeBuilder = routeBuilderImplProvider.get().PUT();
        allRouteBuilders.add(routeBuilder);

        return routeBuilder;
    }

    @Override
    public RouteBuilder DELETE() {
        RouteBuilderImpl routeBuilder = routeBuilderImplProvider.get().DELETE();
        allRouteBuilders.add(routeBuilder);

        return routeBuilder;
    }

    @Override
    public RouteBuilder OPTIONS() {
        RouteBuilderImpl routeBuilder = routeBuilderImplProvider.get().OPTIONS();
        allRouteBuilders.add(routeBuilder);

        return routeBuilder;
    }

    @Override
    public RouteBuilder HEAD() {
        RouteBuilderImpl routeBuilder = routeBuilderImplProvider.get().HEAD();
        allRouteBuilders.add(routeBuilder);

        return routeBuilder;
    }

    @Override
    public RouteBuilder METHOD(String method) {
        RouteBuilderImpl routeBuilder = routeBuilderImplProvider.get().METHOD(method);
        allRouteBuilders.add(routeBuilder);

        return routeBuilder;
    }

    private void verifyRoutesCompiled() {
        if (routes == null) {
            throw new IllegalStateException(
                    "Routes not compiled!");
        }
    }

    @Override
    public Optional<Route> getRouteForControllerClassAndMethod(
            Class<?> controllerClass,
            String controllerMethodName) {

        verifyRoutesCompiled();

        MethodReference reverseRouteKey
                = new MethodReference(controllerClass, controllerMethodName);

        Route route = this.reverseRoutes.get(reverseRouteKey);

        return Optional.ofNullable(route);
    }

    @Override
    public Router subRouter(String path) {
        return new SubRouter(this, path);
    }

    private static class SubRouter implements Router {

        private final Router rootRouter;

        private final String prefix;

        public SubRouter(Router rootRouter, String prefix) {
            this.rootRouter = rootRouter;
            this.prefix = prefix;
        }

        @Override
        public Router subRouter(String path) {
            return new SubRouter(rootRouter, StringUtils.removeEnd(prefix, "/") + path);
        }

        @Override
        public RouteBuilder GET() {
            RouteBuilderImpl routeBuilder = (RouteBuilderImpl) rootRouter.GET();
            return routeBuilder.prefix(prefix);
        }

        @Override
        public RouteBuilder WS() {
            RouteBuilderImpl routeBuilder = (RouteBuilderImpl) rootRouter.WS();
            return routeBuilder.prefix(prefix);
        }

        @Override
        public RouteBuilder POST() {
            RouteBuilderImpl routeBuilder = (RouteBuilderImpl) rootRouter.POST();

            return routeBuilder.prefix(prefix);
        }

        @Override
        public RouteBuilder PUT() {
            RouteBuilderImpl routeBuilder = (RouteBuilderImpl) rootRouter.PUT();

            return routeBuilder.prefix(prefix);
        }

        @Override
        public RouteBuilder DELETE() {
            RouteBuilderImpl routeBuilder = (RouteBuilderImpl) rootRouter.GET();

            return routeBuilder.prefix(prefix);
        }

        @Override
        public RouteBuilder OPTIONS() {
            RouteBuilderImpl routeBuilder = (RouteBuilderImpl) rootRouter.OPTIONS();

            return routeBuilder.prefix(prefix);
        }

        @Override
        public RouteBuilder HEAD() {
            RouteBuilderImpl routeBuilder = (RouteBuilderImpl) rootRouter.HEAD();
            return routeBuilder.prefix(prefix);
        }

        @Override
        public RouteBuilder METHOD(String method) {
            RouteBuilderImpl routeBuilder = (RouteBuilderImpl) rootRouter.METHOD(method);

            return routeBuilder.prefix(prefix);
        }
    }

    private void logRoutes() {
        // determine the width of the columns
        int maxMethodLen = 0;
        int maxPathLen = 0;
        int maxResourceLen = 0;

        for (Route route : getRoutes()) {

            maxMethodLen = Math.max(maxMethodLen, route.getHttpMethod().length());
            maxPathLen = Math.max(maxPathLen, route.getUri().length());

            if (route.getControllerClass() != null) {

                int resourceLen = route.getControllerClass().getName().length()
                        + route.getControllerMethod().getName().length();
                maxResourceLen = Math.max(maxResourceLen, resourceLen);

            }

        }

        // log the routing table
        int borderLen = 10 + maxMethodLen + maxPathLen + maxResourceLen;
        String border = Strings.padEnd("", borderLen, '-');

        logger.info(border);
        logger.info("Registered routes");
        logger.info(border);

        for (Route route : getRoutes()) {

            if (route.getControllerClass() != null) {

                logger.info("{} {}  =>  {}.{}()",
                        Strings.padEnd(route.getHttpMethod(), maxMethodLen, ' '),
                        Strings.padEnd(route.getUri(), maxPathLen, ' '),
                        route.getControllerClass().getName(),
                        route.getControllerMethod().getName());

            } else {

                logger.info("{} {}", route.getHttpMethod(), route.getUri());

            }

        }

    }
}