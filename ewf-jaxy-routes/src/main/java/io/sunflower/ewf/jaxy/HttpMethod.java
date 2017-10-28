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
package io.sunflower.ewf.jaxy;

import java.lang.annotation.*;

/**
 * @author James Moger
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpMethod {

    /**
     * HTTP GET method.
     */
    String GET = "GET";
    /**
     * HTTP PATCH method.
     */
    String PATCH = "PATCH";
    /**
     * HTTP POST method.
     */
    String POST = "POST";
    /**
     * HTTP PUT method.
     */
    String PUT = "PUT";
    /**
     * HTTP DELETE method.
     */
    String DELETE = "DELETE";
    /**
     * HTTP HEAD method.
     */
    String HEAD = "HEAD";
    /**
     * HTTP OPTIONS method.
     */
    String OPTIONS = "OPTIONS";

    /**
     * Specifies the name of a HTTP method. E.g. "GET".
     */
    String value();
}