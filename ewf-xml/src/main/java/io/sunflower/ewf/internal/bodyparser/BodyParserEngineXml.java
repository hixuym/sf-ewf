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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.errors.BadRequestException;
import io.sunflower.ewf.spi.BodyParserEngine;

import java.io.IOException;

/**
 * Built in Xml body parser.
 *
 * @author Raphael Bauer
 * @author Thibault Meyer
 * @see BodyParserEngine
 */
@Singleton
public class BodyParserEngineXml implements BodyParserEngine {

    private final XmlMapper xmlMapper;

    @Inject
    public BodyParserEngineXml(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    @Override
    public <T> T invoke(Context context, Class<T> classOfT) {
        try {
            return xmlMapper.readValue(context.getInputStream(), classOfT);
        } catch (JsonParseException | JsonMappingException e) {
            throw new BadRequestException("Error parsing incoming Xml", e);
        } catch (IOException e) {
            throw new BadRequestException("Invalid Xml document", e);
        }
    }

    @Override
    public String getContentType() {
        return Result.APPLICATION_XML;
    }

}
