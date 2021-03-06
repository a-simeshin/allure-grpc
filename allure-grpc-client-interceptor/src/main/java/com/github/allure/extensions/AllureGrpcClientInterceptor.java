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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;

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
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
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
                                new StepResult().setName("gRPC interaction " + methodDescriptor.getFullMethodName()));
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
                                                    stepResult ->
                                                            stepResult.setStatus(io.qameta.allure.model.Status.PASSED));
                                } else {
                                    Allure.getLifecycle()
                                            .updateStep(
                                                    allureStepUUID,
                                                    stepResult ->
                                                            stepResult.setStatus(io.qameta.allure.model.Status.FAILED));
                                }
                                Allure.getLifecycle().stopStep(allureStepUUID);

                                super.onClose(status, trailers);
                            }
                        },
                        headers);
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
