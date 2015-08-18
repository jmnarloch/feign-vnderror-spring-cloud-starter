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

import feign.Feign;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the vnd.error decoding whenever the Netflix Feign is on classpath and Feign clients has been enabled.
 *
 * @author Jakub Narloch
 * @see VndErrorDecoder
 * @see VndErrorException
 */
@Configuration
@ConditionalOnClass(Feign.class)
@ConditionalOnBean(FeignClientsConfiguration.class)
public class VndErrorAutoConfiguration {

    /**
     * Enables the custom error decoder.
     *
     * @return the error decoder
     */
    @Bean
    @ConditionalOnMissingBean
    public ErrorDecoder vndErrorDecoder() {
        return new VndErrorDecoder();
    }
}
