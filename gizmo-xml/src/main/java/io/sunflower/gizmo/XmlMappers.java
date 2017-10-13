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

package io.sunflower.gizmo;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

/**
 * XmlMappers
 *
 * @author michael
 * @date 17/10/13 22:30
 */
public class XmlMappers {

  public static XmlMapper newXmlMapper() {
    JacksonXmlModule xmlModule = new JacksonXmlModule();

    // Check out: https://github.com/FasterXML/jackson-dataformat-xml
    // setDefaultUseWrapper produces more similar output to
    // the Json output. You can change that with annotations in your
    // models.
    xmlModule.setDefaultUseWrapper(false);

    XmlMapper xmlMapper = new XmlMapper(xmlModule);

    xmlMapper.registerModule(new AfterburnerModule());

    return xmlMapper;
  }
}
