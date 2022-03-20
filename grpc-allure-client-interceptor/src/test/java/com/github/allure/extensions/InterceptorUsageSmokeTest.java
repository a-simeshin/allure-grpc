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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.github.allure.extensions.config.ClientTestConfiguration;
import com.github.allure.extensions.config.GrpcServerEmulator;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@EnableAutoConfiguration
@SpringBootTest(
        classes = ClientTestConfiguration.class,
        properties = {
            "grpc.server.port=0",
            "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
            "grpc.client.testing.address=self:self"
        })
public class InterceptorUsageSmokeTest {

    @Autowired
    GreeterGrpc.GreeterBlockingStub greeterBlockingStub;

    @Autowired
    GrpcServerEmulator grpcServerEmulator;

    final HelloRequest request = HelloRequest.newBuilder().setName("Smoke").build();

    @Test
    public void smokeSingleMessageMethodTest() {
        grpcServerEmulator.setReturnError(false);
        Allure.addAttachment("message to send", request.toString());
        Allure.step("Send message", () -> assertDoesNotThrow(() -> greeterBlockingStub.sayHello(request)));
    }

    @Test
    public void smokeSingleStreamedMethodTest() {
        grpcServerEmulator.setReturnError(false);
        Allure.addAttachment("message to send", request.toString());
        Allure.step("Send message", () -> assertDoesNotThrow(() -> greeterBlockingStub.sayHelloStream(request)));
    }
}
