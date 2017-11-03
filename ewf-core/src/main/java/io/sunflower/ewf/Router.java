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

import io.sunflower.ewf.internal.RouteBuilder;

/**
 * @author michael
 */
public interface Router {

    /**
     * sub router serving prefix with path
     *
     * @param path
     * @return
     */
    Router subRouter(String path);

    /**
     * convenience methods to use the route in a DSL like way router.GET().route("/index").with(.....)
     */
    RouteBuilder GET();

    /**
     * return websocket route builder
     */
    RouteBuilder WS();

    /**
     * return post method route builder
     */
    RouteBuilder POST();

    /**
     * get put method route builder
     */
    RouteBuilder PUT();

    /**
     * get delete method route builder
     */
    RouteBuilder DELETE();

    /**
     * get options method route builder
     */
    RouteBuilder OPTIONS();

    /**
     * get head method route builder
     */
    RouteBuilder HEAD();

    /**
     * To match any http method. E.g. METHOD("PROPFIND") would route PROPFIND methods.
     *
     * @param method The http method like "GET" or "PROPFIND"
     * @return the routeBuilder for chaining.
     */
    RouteBuilder METHOD(String method);
}