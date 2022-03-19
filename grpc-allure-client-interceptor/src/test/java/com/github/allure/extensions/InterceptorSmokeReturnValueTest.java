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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
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
        Allure.step("Send message then check", () -> {
            final HelloReply reply = greeterBlockingStub.sayHello(request);
            assertNotNull(reply);
            assertEquals("Hi Smoke", reply.getMessage());
        });
    }

    @Test
    public void smokeStreamedMessagesMethodTest() {
        Allure.addAttachment("message to send", request.toString());
        Allure.step("Send message then check", () -> {
            final List<HelloReply> expecting = new ArrayList<>();
            expecting.add(HelloReply.newBuilder().setMessage("Hi Smoke").build());
            expecting.add(HelloReply.newBuilder().setMessage("And again Hi Smoke").build());

            final List<HelloReply> replyList = new ArrayList<>();
            final Iterator<HelloReply> reply = greeterBlockingStub.sayHelloStream(request);
            while (reply.hasNext()) {
                replyList.add(reply.next());
            }
            assertArrayEquals(expecting.toArray(), replyList.toArray());
        });
    }

}
