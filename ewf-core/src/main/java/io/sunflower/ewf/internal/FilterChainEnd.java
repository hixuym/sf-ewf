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

package io.sunflower.ewf.internal;

import com.google.inject.Provider;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.FilterChain;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.params.internal.ControllerMethodInvoker;

/**
 * The end of the filter chain
 *
 * @author James Roper
 */
class FilterChainEnd implements FilterChain {

    private final Provider<?> targetObjectProvider;
    private final ControllerMethodInvoker controllerMethodInvoker;

    FilterChainEnd(Provider<?> targetObjectProvider,
                   ControllerMethodInvoker controllerMethodInvoker) {
        this.targetObjectProvider = targetObjectProvider;
        this.controllerMethodInvoker = controllerMethodInvoker;
    }

    @Override
    public Result next(Context context) {
        return (Result) controllerMethodInvoker.invoke(targetObjectProvider.get(), context);
    }
}