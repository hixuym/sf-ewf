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

package io.sunflower.ewf.spi.internal;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.errors.BadRequestException;
import io.sunflower.ewf.internal.Route;
import io.sunflower.ewf.spi.RouteHandlerImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author michael
 * created on 17/6/23
 */
@Singleton
public class InstrumentedRouteHandler extends RouteHandlerImpl {

    private static final String METER_ALL_REQUESTS = "ewf.requests.allRequests";
    private static final String COUNTER_ACTIVE_REQUESTS = "ewf.requests.activeRequests";
    private static final String METER_BAD_REQUESTS = "ewf.requests.badRequests";
    private static final String METER_INTERNAL_SERVER_ERRORS = "ewf.requests.internalServerErrors";
    private static final String METER_ROUTES_NOT_FOUND = "ewf.requests.routesNotFound";

    private Meter allRequestsMeter;
    private Counter activeRequests;
    private Meter badRequests;
    private Meter internalServerErrors;
    private Meter routesNotFound;

    private final MetricRegistry metricRegistry;

    @Inject
    public InstrumentedRouteHandler(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        allRequestsMeter = metricRegistry.meter(METER_ALL_REQUESTS);
        activeRequests = metricRegistry.counter(COUNTER_ACTIVE_REQUESTS);
        badRequests = metricRegistry.meter(METER_BAD_REQUESTS);
        internalServerErrors = metricRegistry.meter(METER_INTERNAL_SERVER_ERRORS);
        routesNotFound = metricRegistry.meter(METER_ROUTES_NOT_FOUND);
    }

    @Override
    public void handleRequest(Context.Impl context) {

        Timer timer = metricRegistry.timer("ewf.routeRequest");

        Timer.Context timerContext = timer.time();

        try {
            activeRequests.inc();

            String httpMethod = context.getMethod();

            Route route = router.getRouteFor(httpMethod, context.getRequestPath());

            if (route == null) {
                routesNotFound.mark();
                Result result = exceptionHandler.getNotFoundResult(context);
                renderErrorResultAndCatchAndLogExceptions(result, context);
                return;
            }

            allRequestsMeter.mark();
            context.setRoute(route);

            Result underlyingResult;
            try {
                underlyingResult = route.getFilterChain().next(context);
                resultHandler.handleResult(underlyingResult, context);
            } catch (Exception exception) {
                if (exception instanceof BadRequestException) {
                    badRequests.mark();
                } else {
                    internalServerErrors.mark();
                }
                Result result = exceptionHandler.onException(context, exception);
                renderErrorResultAndCatchAndLogExceptions(result, context);
            } finally {
                context.cleanup();
                activeRequests.dec();
            }
        } finally {
            timerContext.stop();
        }
    }
}
