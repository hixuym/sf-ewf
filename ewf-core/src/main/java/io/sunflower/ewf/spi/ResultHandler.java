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

package io.sunflower.ewf.spi;

import com.google.inject.ImplementedBy;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.spi.support.ResultHandlerImpl;

/**
 * ResultHandlerImpl
 *
 * @author michael
 * created on 17/11/3 15:39
 */
@ImplementedBy(ResultHandlerImpl.class)
public interface ResultHandler {

    /**
     * handle controller returned result.
     *
     * @param result
     * @param context
     */
    void handleResult(Result result, Context context);
}
