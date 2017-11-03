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

package io.sunflower.ewf.assets;

import io.sunflower.ewf.ApplicationRoutes;
import io.sunflower.ewf.Router;
import io.sunflower.ewf.internal.Route;

/**
 * AssetsRoutes
 *
 * @author michael created on 17/10/20 17:48
 */
public class AssetsRoutes implements ApplicationRoutes {

    @Override
    public void init(Router router) {

        Router subRouter = router.subRouter("/assets");

        ///////////////////////////////////////////////////////////////////////
        // Assets (pictures / javascript)
        ///////////////////////////////////////////////////////////////////////
        subRouter.GET().route("/webjars/{fileName: .*}").with(AssetsController::serveWebJars);
        subRouter.GET().route("/{fileName: .*}").with(AssetsController::serveStatic);
    }

    @Override
    public int order() {
        // assets route at last.
        return 100;
    }
}
