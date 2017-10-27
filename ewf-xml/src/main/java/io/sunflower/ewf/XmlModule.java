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

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.inject.AbstractModule;
import io.sunflower.ewf.internal.bodyparser.BodyParserEngineXml;
import io.sunflower.ewf.internal.template.TemplateEngineXml;

/**
 * XmlModule
 *
 * @author michael
 * created on 17/10/13 22:32
 */
public class XmlModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(XmlMapper.class).toInstance(XmlMappers.newXmlMapper());
    bind(BodyParserEngineXml.class);
    bind(TemplateEngineXml.class);
  }

}
