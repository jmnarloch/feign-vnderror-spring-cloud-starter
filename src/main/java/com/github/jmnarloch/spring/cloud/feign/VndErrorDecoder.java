/**
 * Copyright (c) 2015 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jmnarloch.spring.cloud.feign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.VndErrors;
import org.springframework.hateoas.VndErrors.VndError;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

/**
 * A a custom error decoder capable of instantiating {@link VndErrorException}. The decoder will try to match any
 * responses matching the {@code application/vnd.error+json} content types and unmarshall the {@link VndErrors}
 * instance. If that fails it will make a second attempt to retrieve single {@link VndError} out of the response body.
 * Afterwards the unmarshalled error object will wrapped into {@link VndErrorException} and propagated by Feign.
 *
 * @author Jakub Narloch
 * @see VndErrors
 * @see VndErrorException
 * @see <a href="https://github.com/blongden/vnd.error">https://github.com/blongden/vnd.error</a>
 */
public class VndErrorDecoder implements ErrorDecoder, InitializingBean {

    /**
     * Logger instance used by this class.
     */
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * The content type header.
     */
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * The expected JSON vnd.error media type.
     */
    private static final String JSON_VND_ERROR_MEDIA_TYPE = "application/vnd.error+json";

    /**
     * The optional instance of the Jackson {@link ObjectMapper}, if non has been configured a new instance
     * will be created.
     */
    @Autowired(required = false)
    private ObjectMapper objectMapper;

    /**
     * Initializes all needed properties.
     *
     * @throws Exception if any error occurs
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    /**
     * Decodes the JSON vnd.error out of the response payload, if case that no matching content type has been found
     * fallbacks to the default decoder.
     *
     * @param methodKey the method key
     * @param response  the response object
     * @return the decoded exception
     */
    @Override
    public Exception decode(String methodKey, Response response) {

        try {
            if (hasVndError(response)) {

                return decodeVndError(response);
            }

            return new ErrorDecoder.Default().decode(methodKey, response);
        } catch (IOException e) {
            logger.error("An unexpected error occurred during vnd.error decoding", e);
            throw FeignException.errorStatus(methodKey, response);
        }
    }

    /**
     * Returns true if the response contains vnd.error as the body payload.
     *
     * @param response the response object
     * @return {@code true} if response contains vnd.error, {@code false} otherwise
     */
    private boolean hasVndError(Response response) {
        Collection<String> contentTypes = response.headers().get(CONTENT_TYPE_HEADER);
        return contentTypes != null && contains(contentTypes, JSON_VND_ERROR_MEDIA_TYPE);
    }

    /**
     * Decodes the vnd.error out of the response body.
     *
     * @return the decoded exception
     * @throws IOException if any error occurs during response processing
     */
    private Exception decodeVndError(Response response) throws IOException {

        VndErrors vndErrors = null;
        final byte[] body = body(response);

        try {
            vndErrors = reader(VndErrors.class).readValue(body);
        } catch (JsonProcessingException e) {
            // ignores exception
        }

        if (vndErrors == null) {
            final VndError vndError = reader(VndError.class).readValue(body);
            vndErrors = new VndErrors(vndError);
        }

        return new VndErrorException(vndErrors);
    }

    /**
     * Returns the object reader for specified type.
     *
     * @param expectedType the type to unmarshall
     * @return the object reader for specified type
     */
    private ObjectReader reader(Class<?> expectedType) {

        return objectMapper.reader(expectedType);
    }

    /**
     * Reads the entire response body content and returns it as byte array.
     *
     * @param response the response object
     * @return the body content
     * @throws IOException if any error occurs during response processing
     */
    private static byte[] body(Response response) throws IOException {
        try (Response.Body body = response.body()) {
            return IOUtils.toByteArray(body.asInputStream());
        }
    }

    /**
     * Returns whether within the specified collection any string contains a specific substring.
     *
     * @param collection the collections of strings
     * @param pattern    the pattern to search
     * @return {@code true} if any entry in collections contains a {@code pattern}, {@code false} otherwise
     */
    private static boolean contains(Collection<String> collection, String pattern) {
        for (String entry : collection) {
            if (entry.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
