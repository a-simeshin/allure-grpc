package com.github.allure.extensions;

import com.github.allure.extensions.config.ClientTestConfiguration;
import com.github.allure.extensions.config.GrpcServerEmulator;
import io.qameta.allure.model.Attachment;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.test.AllureResults;
import io.qameta.allure.test.RunUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    /*
     * Step Result tests ----------------------------------------------------------------------------------------------
     */
    @Test
    public void stepResultForUnaryTest() {
        final AllureResults allureResults = executeUnary();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        assertEquals(
                Status.PASSED,
                stepResult.getStatus(),
                "negative step result is correct for interactions with error"
        );
    }

    @Test
    public void stepResultForStreamTest() {
        final AllureResults allureResults = executeStream();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        assertEquals(
                Status.PASSED,
                stepResult.getStatus(),
                "negative step result is correct for interactions with error"
        );
    }

    @Test
    public void stepResultNegativeForUnaryTest() {
        grpcServerEmulator.setReturnError(true);
        final AllureResults allureResults = executeUnary();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        assertEquals(
                Status.FAILED,
                stepResult.getStatus(),
                "negative step result is correct for interactions with error"
        );
    }

    @Test
    public void stepResultNegativeForStreamTest() {
        grpcServerEmulator.setReturnError(true);
        final AllureResults allureResults = executeStream();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        assertEquals(
                Status.FAILED,
                stepResult.getStatus(),
                "negative step result is correct for interactions with error"
        );
    }

    /*
     * Step name tests ------------------------------------------------------------------------------------------------
     */
    @Test
    public void stepNameForUnaryTest() {
        final AllureResults allureResults = executeUnary();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        assertEquals(
                "gRPC interaction com.github.allure.extensions.Greeter/SayHello",
                stepResult.getName(),
                "full method descriptor in the step name"
        );
    }

    @Test
    public void stepNameForStreamTest() {
        final AllureResults allureResults = executeStream();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        assertEquals(
                "gRPC interaction com.github.allure.extensions.Greeter/SayHelloStream",
                stepResult.getName(),
                "full method descriptor in the step name"
        );
    }

    /*
     * Method descriptor tests ----------------------------------------------------------------------------------------
     */
    @Test
    public void methodDescriptorAttachmentForUnaryTest() {
        final AllureResults allureResults = executeUnary();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        final List<Attachment> attachmentList = stepResult.getAttachments();
        assertTrue(
                attachmentList.stream().anyMatch(x -> x.getName().equals("gRPC method")),
                "interceptor attached gRPC method descriptor"
        );
    }

    @Test
    public void methodDescriptorAttachmentForStreamTest() {
        final AllureResults allureResults = executeStream();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        final List<Attachment> attachmentList = stepResult.getAttachments();
        assertTrue(
                attachmentList.stream().anyMatch(x -> x.getName().equals("gRPC method")),
                "interceptor attached gRPC method descriptor"
        );
    }

    /*
     * Request payload tests ------------------------------------------------------------------------------------------
     */
    @Test
    public void requestPayloadAttachmentForUnaryTest() {
        final AllureResults allureResults = executeUnary();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        final List<Attachment> attachmentList = stepResult.getAttachments();
        assertTrue(
                attachmentList.stream().anyMatch(x -> x.getName().equals("gRPC request")),
                "interceptor attached request message"
        );
    }

    @Test
    public void requestPayloadAttachmentForStreamTest() {
        final AllureResults allureResults = executeStream();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        final List<Attachment> attachmentList = stepResult.getAttachments();
        assertTrue(
                attachmentList.stream().anyMatch(x -> x.getName().equals("gRPC request")),
                "interceptor attached request message"
        );
    }

    /*
     * Headers tests --------------------------------------------------------------------------------------------------
     */
    @Test
    public void headersAttachmentForUnaryTest() {
        final AllureResults allureResults = executeUnary();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        final List<Attachment> attachmentList = stepResult.getAttachments();
        assertTrue(
                attachmentList.stream().anyMatch(x -> x.getName().equals("gRPC headers")),
                "interceptor attached gRPC header"
        );
    }

    @Test
    public void headersAttachmentForStreamTest() {
        final AllureResults allureResults = executeStream();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        final List<Attachment> attachmentList = stepResult.getAttachments();
        assertTrue(
                attachmentList.stream().anyMatch(x -> x.getName().equals("gRPC headers")),
                "interceptor attached gRPC header"
        );
    }

    /*
     * Response payloads tests ----------------------------------------------------------------------------------------
     */
    @Test
    public void responsePayloadAttachmentForUnaryTest() {
        final AllureResults allureResults = executeUnary();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        final List<Attachment> attachmentList = stepResult.getAttachments();
        assertTrue(
                attachmentList.stream().anyMatch(x -> x.getName().equals("gRPC responses")),
                "interceptor attached gRPC responses"
        );
    }

    @Test
    public void responsePayloadsAttachmentForStreamTest() {
        final AllureResults allureResults = executeStream();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        final List<Attachment> attachmentList = stepResult.getAttachments();
        assertTrue(
                attachmentList.stream().anyMatch(x -> x.getName().equals("gRPC responses")),
                "interceptor attached gRPC responses"
        );
    }

    /*
     * Interaction status tests ---------------------------------------------------------------------------------------
     */
    @Test
    public void interactionStatusAttachmentForUnaryTest() {
        final AllureResults allureResults = executeUnary();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        final List<Attachment> attachmentList = stepResult.getAttachments();
        assertTrue(
                attachmentList.stream().anyMatch(x -> x.getName().equals("gRPC status")),
                "interceptor attached gRPC status"
        );
    }

    @Test
    public void interactionStatusAttachmentForStreamTest() {
        final AllureResults allureResults = executeStream();
        final StepResult stepResult = allureResults.getTestResults().get(0).getSteps().get(0);
        final List<Attachment> attachmentList = stepResult.getAttachments();
        assertTrue(
                attachmentList.stream().anyMatch(x -> x.getName().equals("gRPC status")),
                "interceptor attached gRPC status"
        );
    }

    /*
     * Util methods ---------------------------------------------------------------------------------------------------
     */
    private AllureResults executeUnary() {
        final AllureResults allureResults = RunUtils.runWithinTestContext(() -> greeterBlockingStub.sayHello(request));
        assertEquals(1, allureResults.getTestResults().size(), "1 test result exist for 1 test");
        assertEquals(1, allureResults.getTestResults().get(0).getSteps().size(),
                "1 step result exist for 1 unary call through interceptor");
        return allureResults;
    }

    private AllureResults executeStream() {
        final AllureResults allureResults = RunUtils.runWithinTestContext(
                () -> {
                    final Iterator<HelloReply> helloReplyIterator = greeterBlockingStub.sayHelloStream(request);
                    while (helloReplyIterator.hasNext()) {
                        helloReplyIterator.next();
                    }
                }
        );
        assertEquals(1, allureResults.getTestResults().size(), "1 test result exist for 1 test");
        assertEquals(1, allureResults.getTestResults().get(0).getSteps().size(),
                "1 step result exist for 1 unary call through interceptor");
        return allureResults;
    }
}
