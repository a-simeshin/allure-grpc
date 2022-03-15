package com.github.allure.extensions;

import com.google.protobuf.Message;
import io.grpc.*;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.util.ObjectUtils;
import org.awaitility.Awaitility;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * With this interceptor grpc client will attach all interaction data to Allure report.
 *
 * @see ClientInterceptor
 */
public class AllureGrpcClientInterceptor implements ClientInterceptor {

    private static InheritableThreadLocal<AllureLifecycle> lifecycle = new InheritableThreadLocal<AllureLifecycle>() {
        @Override
        protected AllureLifecycle initialValue() {
            return Allure.getLifecycle();
        }
    };

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions,
                                                               Channel channel) {
        final ClientCall<ReqT, RespT> call = channel.newCall(methodDescriptor, callOptions);

        /*
         * Returning wrapper for original delegated client call with some logic for allure attachments
         */
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
            final AtomicBoolean interactionIsDone = new AtomicBoolean(false);
            final AtomicReference<List<RespT>> responses = new AtomicReference<>(new ArrayList<>());
            final AtomicReference<Metadata> metadata = new AtomicReference<>(null);
            final AtomicReference<Status> interactionStatus = new AtomicReference<>();

            /**
             * Attaching all request data instantly and async-attaching responses, headers and status
             *
             * @param message request proto message
             */
            @Override
            @SuppressWarnings("unchecked")
            public void sendMessage(ReqT message) {
                super.sendMessage(message);

                Allure.setLifecycle(getLifecycle());
                Allure.step("gRPC intercation " + methodDescriptor.getServiceName(), () -> {
                    Allure.addAttachment("gRPC method", ObjectUtils.toString(methodDescriptor));
                    Allure.addAttachment("gRPC request", ObjectUtils.toString(ProtoFormatter.format((Message) message)));
                    Allure.addByteAttachmentAsync("gRPC responses", "text/plain", () -> {
                        Awaitility.await().until(interactionIsDone::get);
                        return ObjectUtils.toString(ProtoFormatter.format((List<Message>) responses.get()))
                                .getBytes(StandardCharsets.UTF_8);
                    });
                    Allure.addByteAttachmentAsync("gRPC status", "text/plain", () -> {
                        Awaitility.await().until(() -> interactionStatus.get() != null);
                        return ObjectUtils.toString(interactionStatus.get()).getBytes(StandardCharsets.UTF_8);
                    });
                });
            }

            /**
             * Declaring listener to update references in top-leve containers on any changes
             * @param responseListener delegated listener
             * @param headers interaction headers
             */
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            public void onHeaders(Metadata headers) {
                                metadata.set(headers);
                                super.onHeaders(headers);
                            }

                            @Override
                            public void onMessage(RespT message) {
                                responses.get().add(message);
                                super.onMessage(message);
                            }

                            @Override
                            public void onClose(Status status, Metadata trailers) {
                                interactionStatus.set(status);
                                interactionIsDone.set(true);
                                super.onClose(status, trailers);
                            }
                        }, headers);
            }
        };
    }

    /**
     * For test purposes only
     *
     * @return current Allure lifecycle for chek attachments data
     */
    public static AllureLifecycle getLifecycle() {
        return lifecycle.get();
    }

}
