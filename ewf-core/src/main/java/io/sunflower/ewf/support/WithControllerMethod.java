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
package io.sunflower.ewf.support;

import io.sunflower.ewf.support.ControllerMethods.*;

/**
 * Interface that exposes multiple with methods that accept a large number of various argument
 * combinations.
 *
 * @author michael
 * @param <T> The result to return
 */
public interface WithControllerMethod<T> {

    T with(ControllerMethod controllerMethod);

    default T with(ControllerMethod0 resourceMethod0) {
        return with((ControllerMethod) resourceMethod0);
    }

    default <A> T with(ControllerMethod1<A> resourceMethod1) {
        return with((ControllerMethod) resourceMethod1);
    }

    default <A, B> T with(ControllerMethod2<A, B> resourceMethod2) {
        return with((ControllerMethod) resourceMethod2);
    }

    default <A, B, C> T with(ControllerMethod3<A, B, C> resourceMethod3) {
        return with((ControllerMethod) resourceMethod3);
    }

    default <A, B, C, D> T with(ControllerMethod4<A, B, C, D> resourceMethod4) {
        return with((ControllerMethod) resourceMethod4);
    }

    default <A, B, C, D, E> T with(ControllerMethod5<A, B, C, D, E> resourceMethod5) {
        return with((ControllerMethod) resourceMethod5);
    }

    default <A, B, C, D, E, F> T with(
            ControllerMethod6<A, B, C, D, E, F> resourceMethod6) {
        return with((ControllerMethod) resourceMethod6);
    }

    default <A, B, C, D, E, F, G> T with(ControllerMethod7<A, B, C, D, E, F, G> resourceMethod7) {
        return with((ControllerMethod) resourceMethod7);
    }

    default <A, B, C, D, E, F, G, H> T with(
            ControllerMethod8<A, B, C, D, E, F, G, H> resourceMethod8) {
        return with((ControllerMethod) resourceMethod8);
    }

    default <A, B, C, D, E, F, G, H, I> T with(
            ControllerMethod9<A, B, C, D, E, F, G, H, I> resourceMethod9) {
        return with((ControllerMethod) resourceMethod9);
    }

    default <A, B, C, D, E, F, G, H, I, J> T with(
            ControllerMethod10<A, B, C, D, E, F, G, H, I, J> resourceMethod10) {
        return with((ControllerMethod) resourceMethod10);
    }

}
