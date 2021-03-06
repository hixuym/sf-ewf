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

import com.google.common.net.HttpHeaders;
import io.sunflower.ewf.support.NoHttpBody;

import java.util.Optional;


/**
 * Convenience methods for the generation of Results.
 * <p>
 * {@link Results#forbidden() generates a results and sets it to forbidden.
 * <p>
 * A range of shortcuts are available from here.
 *
 * @author rbauer
 */
public class Results {

    public static Result status(int statusCode) {

        return new Result(statusCode);

    }

    public static Result ok() {
        return status(Result.SC_200_OK);
    }

    public static Result created(Optional<String> url) {
        Result result = status(Result.SC_201_CREATED);

        url.ifPresent(s -> result.addHeader(HttpHeaders.LOCATION, s));

        return result;
    }

    public static Result notFound() {
        return status(Result.SC_404_NOT_FOUND);
    }

    public static Result forbidden() {
        return status(Result.SC_403_FORBIDDEN);
    }

    public static Result unauthorized() {
        return status(Result.SC_401_UNAUTHORIZED);
    }

    public static Result badRequest() {
        return status(Result.SC_400_BAD_REQUEST);
    }

    public static Result noContent() {
        return status(Result.SC_204_NO_CONTENT)
                .render(Result.NO_HTTP_BODY);
    }

    public static Result internalServerError() {
        return status(Result.SC_500_INTERNAL_SERVER_ERROR);
    }

    /**
     * A redirect that uses 303 see other.
     * <p>
     * The redirect does NOT need a template and does NOT render a text in the Http body by default.
     * <p>
     * If you wish to do so please remove the {@link NoHttpBody} that is set as renderable of the
     * Result.
     *
     * @param url The url used as redirect target.
     * @return A nicely configured result with status code 303 and the url set as Location header.
     * Renders no Http body by default.
     */
    public static Result redirect(String url) {

        Result result = status(Result.SC_303_SEE_OTHER);
        result.addHeader(HttpHeaders.LOCATION, url);
        result.render(Result.NO_HTTP_BODY);

        return result;
    }

    /**
     * A redirect that uses 307 see other.
     * <p>
     * The redirect does NOT need a template and does NOT render a text in the Http body by default.
     * <p>
     * If you wish to do so please remove the {@link NoHttpBody} that is set as renderable of the
     * Result.
     *
     * @param url The url used as redirect target.
     * @return A nicely configured result with status code 307 and the url set as Location header.
     * Renders no Http body by default.
     */
    public static Result redirectTemporary(String url) {

        Result result = status(Result.SC_307_TEMPORARY_REDIRECT);
        result.addHeader(HttpHeaders.LOCATION, url);
        result.render(Result.NO_HTTP_BODY);

        return result;
    }

    public static Result contentType(String contentType) {
        Result result = status(Result.SC_200_OK);
        result.contentType(contentType);

        return result;
    }

    public static Result html() {
        Result result = status(Result.SC_200_OK);
        result.contentType(Result.TEXT_HTML);

        return result;
    }

    public static Result text() {
        Result result = status(Result.SC_200_OK);
        result.contentType(Result.TEXT_PLAIN);

        return result;
    }

    public static Result json() {
        return status(Result.SC_200_OK).json();
    }

    public static Result jsonp() {

        return status(Result.SC_200_OK).jsonp();
    }

    public static Result xml() {

        return status(Result.SC_200_OK).xml();
    }

    public static Result TODO() {
        Result result = status(Result.SC_501_NOT_IMPLEMENTED);
        result.contentType(Result.APPLICATION_JSON);

        return result;
    }

}
