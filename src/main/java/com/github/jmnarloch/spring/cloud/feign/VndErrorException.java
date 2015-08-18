/**
 * Copyright (c) 2015 the original author or authors
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jmnarloch.spring.cloud.feign;

import org.springframework.hateoas.VndErrors;

/**
 * The base exception that will be used to propagate any vnd.errors during Feign client invocations.
 *
 * @author Jakub Narloch
 * @see VndErrorDecoder
 * @see VndErrors
 */
public class VndErrorException extends RuntimeException {

    /**
     * The response http status.
     */
    private int status;

    /**
     * The vnd error.
     */
    private final VndErrors vndErrors;

    /**
     * Creates new instance of {@link VndErrorException} with the response status and the {@link VndErrors} details.
     *
     * @param status the http status
     * @param vndErrors the errors details
     */
    public VndErrorException(int status, VndErrors vndErrors) {
        this.status = status;
        this.vndErrors = vndErrors;
    }

    /**
     * Creates new instance of {@link VndErrorException} with the detailed error message, response status and
     * {@link VndErrors} details.
     *
     * @param message   the detailed error message
     * @param status the http status
     * @param vndErrors the error details
     */
    public VndErrorException(String message, int status, VndErrors vndErrors) {
        super(message);
        this.status = status;
        this.vndErrors = vndErrors;
    }

    /**
     * Retrieves the response http status.
     *
     * @return the response http status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Retrieves the vnd errors.
     *
     * @return the vnd errors
     */
    public VndErrors getVndErrors() {
        return vndErrors;
    }
}
