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

package io.sunflower.ewf.spi.support;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import com.google.inject.Inject;
import io.sunflower.ewf.AsyncResult;
import io.sunflower.ewf.Context;
import io.sunflower.ewf.Renderable;
import io.sunflower.ewf.Result;
import io.sunflower.ewf.errors.BadRequestException;
import io.sunflower.ewf.errors.WebApplicationException;
import io.sunflower.ewf.i18n.Messages;
import io.sunflower.ewf.internal.template.TemplateEngineManager;
import io.sunflower.ewf.spi.ResultHandler;
import io.sunflower.ewf.spi.TemplateEngine;
import io.sunflower.ewf.support.NoHttpBody;
import io.sunflower.ewf.validation.ConstraintViolation;
import io.sunflower.ewf.validation.ValidationErrorMessage;

import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;


/**
 * @author michael
 */
@Singleton
public class ResultHandlerImpl implements ResultHandler {

    private final TemplateEngineManager templateEngineManager;
    private final Messages messages;

    @Inject
    public ResultHandlerImpl(TemplateEngineManager templateEngineManager,
                             Messages messages) {
        this.templateEngineManager = templateEngineManager;
        this.messages = messages;
    }

    @Override
    public void handleResult(Result result, Context context) {

        if (result == null || result instanceof AsyncResult) {
            // Do nothing, assuming the controller manually handled it
            return;
        }

        // restfull bad request
        if (result.getStatusCode() == Result.SC_400_BAD_REQUEST
                && context.getValidation().hasViolations()) {

            List<ConstraintViolation> violations = context.getValidation().getViolations();
            ImmutableList.Builder<String> errorsBuilder = ImmutableList.builder();

            for (ConstraintViolation violation : violations) {

                String msg = messages.get(violation.getMessageKey(),
                        context,
                        Optional.of(result),
                        violation.getMessageParams()).orElse(violation.getDefaultMessage());

                errorsBuilder.add(msg);
            }

            result.json().render(new ValidationErrorMessage(errorsBuilder.build()));
        }

        Object objectToBeRendered = result.getRenderable();

        if (objectToBeRendered instanceof Renderable) {
            // if the object is a renderable it should do everything itself...:
            // make sure to call context.finalizeHeaders(result) with the
            // results you want to set...
            ((Renderable) objectToBeRendered).render(context, result);
        } else {

            // If result does not contain a Cache-control: ... header
            // we disable caching of this response by calling doNotCacheContent().
            if (!result.getHeaders().containsKey(HttpHeaders.CACHE_CONTROL)) {
                result.doNotCacheContent();
            }

            if (objectToBeRendered instanceof NoHttpBody) {
                // This indicates that we do not want to render anything in the body.
                // Can be used e.g. for a 204 No Content response.
                // and bypasses the rendering engines.
                context.finalizeHeaders(result);

            } else {
                // normal mode of operation: we render the stuff via the
                // template renderer:
                renderWithTemplateEngineOrRaw(context, result);
            }
        }
    }

    private void renderWithTemplateEngineOrRaw(Context context, Result result) {

        // if content type is not yet set in result we copy it over from the
        // request accept header
        if (result.getContentType() == null) {

            if (result.supportedContentTypes().contains(context.getAcceptContentType())) {
                result.contentType(context.getAcceptContentType());
            } else if (result.fallbackContentType().isPresent()) {
                result.contentType(result.fallbackContentType().get());
            } else {
                throw new BadRequestException(
                        "No idea how to handle incoming request with Accept:"
                                + context.getAcceptContentType()
                                + " at route " + context.getRequestPath());
            }
        }

        // try to get a suitable rendering engine...
        TemplateEngine templateEngine = templateEngineManager
                .getTemplateEngineForContentType(result.getContentType());

        if (templateEngine != null) {

            templateEngine.invoke(context, result);

        } else {
            throw new WebApplicationException(
                    500, "No template engine found for result content type " + result.getContentType());
        }
    }


}
