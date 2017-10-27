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

package io.sunflower.ewf.auth.internal;

import javax.inject.Inject;

import io.sunflower.ewf.Context;
import io.sunflower.ewf.auth.UserManager;
import io.sunflower.ewf.params.ArgumentClassHolder;
import io.sunflower.ewf.params.ArgumentExtractor;

/**
 * CurrentUserExtractor
 *
 * @author michael created on 17/10/27 14:37
 */
public class CurrentUserExtractor<T> implements ArgumentExtractor {

  private final ArgumentClassHolder argumentClassHolder;
  private final UserManager<T> userManager;

  @Inject
  public CurrentUserExtractor(ArgumentClassHolder argumentClassHolder,
      UserManager<T> userManager) {
    this.argumentClassHolder = argumentClassHolder;
    this.userManager = userManager;
  }

  @Override
  public T extract(Context context) {
    return null;
  }

  @Override
  public Class<?> getExtractedType() {
    return argumentClassHolder.getArgumentClass();
  }

  @Override
  public String getFieldName() {
    return null;
  }
}
