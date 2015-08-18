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
     * The vnd error.
     */
    private final VndErrors vndErrors;

    /**
     * Creates new instance of {@link VndErrorException} with the {@link VndErrors} details.
     *
     * @param vndErrors the errors details
     */
    public VndErrorException(VndErrors vndErrors) {
        this.vndErrors = vndErrors;
    }

    /**
     * Creates new instance of {@link VndErrorException} with the detailed error message and {@link VndErrors} details.
     *
     * @param message   the detailed error message
     * @param vndErrors the error details
     */
    public VndErrorException(String message, VndErrors vndErrors) {
        super(message);
        this.vndErrors = vndErrors;
    }

    /**
     * Retrieves the vnd errors
     *
     * @return the vnd errors
     */
    public VndErrors getVndErrors() {
        return vndErrors;
    }
}
