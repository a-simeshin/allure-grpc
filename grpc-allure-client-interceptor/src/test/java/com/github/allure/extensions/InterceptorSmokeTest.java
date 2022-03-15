package com.github.allure.extensions;

import com.github.allure.extensions.config.ClientTestConfiguration;
import com.github.allure.extensions.config.GrpcServerEmulator;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DirtiesContext
@EnableAutoConfiguration
@SpringBootTest(
        classes = ClientTestConfiguration.class,
        properties = {
                "grpc.server.port=0",
                "grpc.client.GLOBAL.negotiationType=PLAINTEXT",
                "grpc.client.testing.address=self:self"
        }
)
public class InterceptorSmokeTest {

    @Autowired
    GreeterGrpc.GreeterBlockingStub greeterBlockingStub;

    @Autowired
    GrpcServerEmulator grpcServerEmulator;

    final HelloRequest request = HelloRequest.newBuilder().setName("Smoke").build();

    @Test
    public void smokeSingleMessageMethodTest() {
        grpcServerEmulator.setReturnError(false);
        Allure.addAttachment("message to send", request.toString());
        Allure.step("Send message", ()-> assertDoesNotThrow(() -> greeterBlockingStub.sayHello(request)));
    }

    @Test
    public void smokeSingleStreamedMethodTest() {
        grpcServerEmulator.setReturnError(false);
        Allure.addAttachment("message to send", request.toString());
        Allure.step("Send message", ()-> assertDoesNotThrow(() -> greeterBlockingStub.sayHelloStream(request)));
    }

}
