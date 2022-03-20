/*
 * MIT License
 *
 * Copyright (c) 2022 a-simeshin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.allure.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.allure.extensions.config.ClientConfig;
import com.github.allure.extensions.config.GrpcServerEmulator;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.test.AllureResults;
import io.qameta.allure.test.RunUtils;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@EnableAutoConfiguration
@SpringBootTest(
        classes = ClientConfig.class,
        properties = {
            "grpc.server.port=0",
            "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
            "grpc.client.testing.address=self:self"
        })
public class AttachToAllureTest {

    @Autowired
    GreeterGrpc.GreeterBlockingStub greeterBlockingStub;

    @Autowired
    GrpcServerEmulator grpcServerEmulator;

    @BeforeEach
    public void setUpServer() {
        grpcServerEmulator.setReturnError(false);
        ProtoFormatter.FORMAT_PROTO_TO_JSON = true;
    }

    final HelloRequest request = HelloRequest.newBuilder().setName("Smoke").build();

    @Test
    public void stepResultForUnaryTest() {
        final AllureResults allureResults = RunUtils.runWithinTestContext(() -> greeterBlockingStub.sayHello(request));
        assertEquals(1, allureResults.getTestResults().size(), "1 test result exist for 1 test");
        assertEquals(
                1,
                allureResults.getTestResults().get(0).getSteps().size(),
                "1 step result exist for 1 unary call through interceptor");

        final StepResult stepResult =
                allureResults.getTestResults().get(0).getSteps().get(0);
        assertEquals(
                Status.PASSED, stepResult.getStatus(), "negative step result is correct for interactions with error");
    }

    @Test
    public void stepResultForStreamTest() {
        final AllureResults allureResults = RunUtils.runWithinTestContext(() -> {
            final Iterator<HelloReply> helloReplyIterator = greeterBlockingStub.sayHelloStream(request);
            while (helloReplyIterator.hasNext()) {
                helloReplyIterator.next();
            }
        });
        assertEquals(1, allureResults.getTestResults().size(), "1 test result exist for 1 test");
        assertEquals(
                1,
                allureResults.getTestResults().get(0).getSteps().size(),
                "1 step result exist for 1 unary call through interceptor");

        final StepResult stepResult =
                allureResults.getTestResults().get(0).getSteps().get(0);
        assertEquals(
                Status.PASSED, stepResult.getStatus(), "negative step result is correct for interactions with error");
    }
}
