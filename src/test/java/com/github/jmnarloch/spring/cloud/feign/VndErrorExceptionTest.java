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

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.hateoas.VndErrors.VndError;

/**
 * Tests the unmarshalling of the vnd errors.
 *
 * @author Jakub Narloch
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = VndErrorExceptionTest.Application.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
@DirtiesContext
public class VndErrorExceptionTest {

    @Value("${local.server.port}")
    private int port = 0;

    @Autowired
    private ErrorsClient errorsClient;

    @Test(expected = VndErrorException.class)
    public void shouldThrowVndErrorException() {

        errorsClient.error();
    }

    @Test
    public void shouldThrowVndErrorAndUnmarshallPayload() {

        VndErrorException exc = callErrorAndCaptureException();
        assertNotNull(exc);
        assertNotNull(exc.getVndErrors());
        assertEquals(1, count(exc.getVndErrors()));
    }

    @Test(expected = VndErrorException.class)
    public void shouldThrowVndErrorsException() {

        errorsClient.errors();
    }

    @Test
    public void shouldThrowVndErrorsAndUnmarshallPayload() {

        VndErrorException exc = callErrorsAndCaptureException();
        assertNotNull(exc);
        assertNotNull(exc.getVndErrors());
        assertEquals(2, count(exc.getVndErrors()));
    }

    @Test(expected = FeignException.class)
    public void shouldIgnoreOtherErrors() {

        errorsClient.unknown();
    }

    private int count(VndErrors vndErrors) {
        int size = 0;
        Iterator<VndError> iter = vndErrors.iterator();
        while (iter.hasNext()) {
            size++;
            iter.next();
        }
        return size;
    }

    private VndErrorException callErrorAndCaptureException() {
        try {
            errorsClient.error();
        } catch (VndErrorException exc) {
            return exc;
        }
        return null;
    }

    private VndErrorException callErrorsAndCaptureException() {
        try {
            errorsClient.errors();
        } catch (VndErrorException exc) {
            return exc;
        }
        return null;
    }

    @FeignClient("errors")
    public interface ErrorsClient {

        @RequestMapping(value = "/vnderror", method = RequestMethod.GET)
        ResponseEntity error();

        @RequestMapping(value = "/vnderrors", method = RequestMethod.GET)
        ResponseEntity errors();

        @RequestMapping(value = "/unknownerror", method = RequestMethod.GET)
        ResponseEntity unknown();
    }

    @Configuration
    @EnableAutoConfiguration
    @RestController
    @EnableFeignClients
    @RibbonClient(name = "errors", configuration = LocalRibbonClientConfiguration.class)
    public static class Application {

        @RequestMapping(value = "/vnderror", method = RequestMethod.GET)
        public ResponseEntity error() {

            final VndError vndError = new VndError(UUID.randomUUID().toString(), "Test error");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.error+json")
                    .body(vndError);
        }

        @RequestMapping(value = "/vnderrors", method = RequestMethod.GET)
        public ResponseEntity errors() {

            final VndErrors vndErrors = new VndErrors(
                    new VndError(UUID.randomUUID().toString(), "First error"),
                    new VndError(UUID.randomUUID().toString(), "Second error")
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header(HttpHeaders.CONTENT_TYPE, "application/vnd.error+json")
                    .body(vndErrors);
        }

        @RequestMapping(value = "/unknownerror", method = RequestMethod.GET)
        public ResponseEntity unknown() {

            throw new RuntimeException("Any other exception");
        }
    }

    @Configuration
    public static class LocalRibbonClientConfiguration {

        @Value("${local.server.port}")
        private int port = 0;

        @Bean
        public ILoadBalancer ribbonLoadBalancer() {
            BaseLoadBalancer balancer = new BaseLoadBalancer();
            balancer.setServersList(Arrays.asList(new Server("localhost", this.port)));
            return balancer;
        }
    }
}