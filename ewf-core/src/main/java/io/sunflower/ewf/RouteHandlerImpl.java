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

import javax.inject.Inject;
import javax.inject.Singleton;

import io.sunflower.ewf.utils.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author michael
 */
@Singleton
public class RouteHandlerImpl implements RouteHandler {

  private static final Logger logger = LoggerFactory.getLogger(RouteHandlerImpl.class);

  @Inject
  protected Router router;

  @Inject
  protected ResultHandler resultHandler;

  @Inject
  protected ExceptionHandler exceptionHandler;

  @Override
  public void handleRequest(Context.Impl context) {

    String httpMethod = context.getMethod();

    Route route = router.getRouteFor(httpMethod, context.getRequestPath());

    if (route == null) {
      // throw a 404 "not found" because we did not find the route
      Result result = exceptionHandler.getNotFoundResult(context);
      renderErrorResultAndCatchAndLogExceptions(result, context);

      return;
    }

    context.setRoute(route);

    Result underlyingResult = null;

    try {

      underlyingResult = route.getFilterChain().next(context);

      resultHandler.handleResult(underlyingResult, context);

    } catch (Exception exception) {
      // call special handler to capture the underlying result if there is one
      Result result = exceptionHandler.onException(exception, context);
      renderErrorResultAndCatchAndLogExceptions(result, context);
    } finally {
      context.cleanup();
    }
  }

  private void renderErrorResultAndCatchAndLogExceptions(Result errorResult, Context context) {
    try {
      resultHandler.handleResult(errorResult, context);
    } catch (Exception exceptionCausingRenderError) {
      logger.error("Unable to handle result. That's really really fishy.", exceptionCausingRenderError);
    }
  }
}
