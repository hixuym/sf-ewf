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

import io.sunflower.ewf.support.WithControllerMethod;

import java.util.List;

/**
 * @author michael
 */
public interface RouteBuilder extends WithControllerMethod<Void> {

    /**
     * setting route uri
     *
     * @param uri
     * @return
     */
    RouteBuilder route(String uri);

    /**
     * ignore global filter for this route
     *
     * @return
     */
    RouteBuilder noGlobalFilters();

    /**
     * override global filters
     *
     * @param filters
     * @return
     */
    RouteBuilder globalFilters(Class<? extends Filter>... filters);

    /**
     * override global filters
     *
     * @param filters
     * @return
     */
    RouteBuilder globalFilters(List<Class<? extends Filter>> filters);

    /**
     * add filters to route
     *
     * @param filters
     * @return
     */
    RouteBuilder filters(Class<? extends Filter>... filters);

    /**
     * add filters to route
     *
     * @param filters
     * @return
     */
    RouteBuilder filters(List<Class<? extends Filter>> filters);

    /**
     * set route handle method
     *
     * @param resourceClass
     * @param resourceMethod
     */
    void with(Class<?> resourceClass, String resourceMethod);
}