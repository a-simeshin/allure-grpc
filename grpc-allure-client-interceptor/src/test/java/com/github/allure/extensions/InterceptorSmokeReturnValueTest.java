package com.github.allure.extensions;

import com.github.allure.extensions.config.ClientTestConfiguration;
import com.github.allure.extensions.config.GrpcServerEmulator;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
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
public class InterceptorSmokeReturnValueTest {

    @Autowired
    GreeterGrpc.GreeterBlockingStub greeterBlockingStub;

    @Autowired
    GrpcServerEmulator grpcServerEmulator;

    @BeforeEach
    public void setUpServer() {
        grpcServerEmulator.setReturnError(false);
    }

    final HelloRequest request = HelloRequest.newBuilder().setName("Smoke").build();

    @Test
    public void smokeSingleMessageMethodTest() {
        Allure.addAttachment("message to send", request.toString());
        Allure.step("Send message then check", ()-> {
            final HelloReply reply = greeterBlockingStub.sayHello(request);
            assertNotNull(reply);
            assertEquals("Hi Smoke", reply.getMessage());
        });
    }

    @Test
    public void smokeStreamedMessagesMethodTest() {
        Allure.addAttachment("message to send", request.toString());
        Allure.step("Send message then check", ()-> {
            final Iterator<HelloReply> reply = greeterBlockingStub.sayHelloStream(request);
            final HelloReply firstReply = reply.next();
            final HelloReply secondReply = reply.next();
            assertAll(
                    () -> assertNotNull(firstReply),
                    () -> assertNotNull(secondReply),
                    () -> assertEquals("Hi Smoke", firstReply.getMessage()),
                    () -> assertEquals("And again Hi Smoke", secondReply.getMessage())
            );
        });
    }

}
