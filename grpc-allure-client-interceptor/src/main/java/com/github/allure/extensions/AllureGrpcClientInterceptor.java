package com.github.allure.extensions;

import com.google.protobuf.Message;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.util.ObjectUtils;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
            private String allureStepUUID;
            private List<Message> payloads;

            /**
             * Attaching all request data instantly and async-attaching responses, headers and status
             *
             * @param message request proto message
             */
            @Override
            @SneakyThrows
            public void sendMessage(ReqT message) {
                allureStepUUID = UUID.randomUUID().toString();
                payloads = new ArrayList<>();
                Allure.getLifecycle()
                        .startStep(
                                allureStepUUID,
                                new StepResult().setName("gRPC interaction " + methodDescriptor.getFullMethodName())
                        );
                Allure.addAttachment("gRPC method", ObjectUtils.toString(methodDescriptor));
                Allure.addAttachment("gRPC request", ObjectUtils.toString(ProtoFormatter.format((Message) message)));
                super.sendMessage(message);
            }

            /**
             * Declaring listener to update references in top-leve containers on any changes
             *
             * @param responseListener delegated listener
             * @param headers          interaction headers
             */
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                            @Override
                            public void onHeaders(Metadata headers) {
                                Allure.addAttachment("gRPC headers", ObjectUtils.toString(headers));
                                super.onHeaders(headers);
                            }

                            @Override
                            public void onMessage(RespT message) {
                                payloads.add((Message) message);
                                super.onMessage(message);
                            }

                            @Override
                            public void onClose(Status status, Metadata trailers) {
                                Allure.addAttachment("gRPC responses", ProtoFormatter.format(payloads));
                                Allure.addAttachment("gRPC status", ObjectUtils.toString(status));

                                if (status.isOk()) {
                                    Allure.getLifecycle()
                                            .updateStep(
                                                    allureStepUUID,
                                                    stepResult -> stepResult.setStatus(io.qameta.allure.model.Status.PASSED)
                                            );
                                } else {
                                    Allure.getLifecycle()
                                            .updateStep(
                                                    allureStepUUID,
                                                    stepResult -> stepResult.setStatus(io.qameta.allure.model.Status.FAILED)
                                            );
                                }
                                Allure.getLifecycle().stopStep(allureStepUUID);

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
