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

package io.sunflower.ewf.template;

import java.io.IOException;
import java.io.Writer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.sunflower.ewf.utils.ResponseStreams;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TemplateEngineText implements TemplateEngine {

  private final Logger logger = LoggerFactory.getLogger(TemplateEngineText.class);

  @Override
  public void invoke(Context context, Result result) {

    ResponseStreams responseStreams = context.finalizeHeaders(result);

    try (Writer outputWriter = responseStreams.getWriter()) {

      outputWriter.write(result.getRenderable().toString());

    } catch (IOException e) {

      logger.error("Error while rendering plain text", e);
    }


  }

  @Override
  public String getContentType() {
    return Result.TEXT_PLAIN;
  }

  @Override
  public String getSuffixOfTemplatingEngine() {
    // intentionally returns null...
    return null;
  }
}