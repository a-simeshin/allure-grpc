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
package com.github.allure.extensions.config;

import com.github.allure.extensions.GreeterGrpc;
import com.github.allure.extensions.HelloReply;
import com.github.allure.extensions.HelloRequest;
import io.grpc.stub.StreamObserver;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * For emulation purposes
 */
@GrpcService
@NoArgsConstructor
public class GrpcServerEmulator extends GreeterGrpc.GreeterImplBase {

    @Setter
    private boolean returnError = false;

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        if (returnError) {
            responseObserver.onError(new RuntimeException("something wrong"));
        } else {
            responseObserver.onNext(HelloReply.newBuilder()
                    .setMessage("Hi " + request.getName())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void sayHelloStream(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        if (returnError) {
            responseObserver.onNext(HelloReply.newBuilder()
                    .setMessage("Hi " + request.getName())
                    .build());
            responseObserver.onError(new RuntimeException("something wrong"));
        } else {
            responseObserver.onNext(HelloReply.newBuilder()
                    .setMessage("Hi " + request.getName())
                    .build());
            responseObserver.onNext(HelloReply.newBuilder()
                    .setMessage("And again Hi " + request.getName())
                    .build());
        }
        responseObserver.onCompleted();
    }
}
