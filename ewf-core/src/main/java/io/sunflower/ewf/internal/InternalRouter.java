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

import io.sunflower.ewf.Router;

import java.util.List;
import java.util.Optional;

/**
 * InternalRouter
 *
 * @author michael
 * created on 17/11/3 13:30
 */
public interface InternalRouter extends Router{

    /**
     * Get the route for the given method and URI
     *
     * @param httpMethod The method
     * @param uri        The URI
     * @return The route
     */
    Route getRouteFor(String httpMethod, String uri);

    /**
     * Compile all the routes that have been registered with the router. This should be called once,
     * during initialization, before the application starts serving requests.
     */
    void compileRoutes();

    /**
     * Returns the list of compiled routes.
     *
     * @return application routes
     */
    List<Route> getRoutes();

    /**
     * get route by resource class and method name.
     *
     * @param controllerClass
     * @param controllerMethodName
     * @return optional route
     */
    Optional<Route> getRouteForControllerClassAndMethod(
            Class<?> controllerClass, String controllerMethodName);

}
