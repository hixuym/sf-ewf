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

package io.sunflower.ewf.internal.bodyparser;

import com.google.inject.ImplementedBy;
import io.sunflower.ewf.spi.BodyParserEngine;

import java.util.Set;

/**
 * @author michael
 */
@ImplementedBy(BodyParserEngineManagerImpl.class)
public interface BodyParserEngineManager {

    /**
     * Returns a set of the registered body parser engine content types.
     *
     * @return the registered content types
     */
    Set<String> getContentTypes();

    /**
     * Find the body parser engine for the given content type
     *
     * @param contentType The content type
     * @return The body parser engine, if found
     */
    BodyParserEngine getBodyParserEngineForContentType(String contentType);

}
