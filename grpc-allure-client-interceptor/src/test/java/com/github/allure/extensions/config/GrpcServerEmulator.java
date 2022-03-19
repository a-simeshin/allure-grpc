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
            responseObserver.onNext(HelloReply.newBuilder().setMessage("Hi " + request.getName()).build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void sayHelloStream(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        if (returnError) {
            responseObserver.onNext(HelloReply.newBuilder().setMessage("Hi " + request.getName()).build());
            responseObserver.onError(new RuntimeException("something wrong"));
        } else {
            responseObserver.onNext(HelloReply.newBuilder().setMessage("Hi " + request.getName()).build());
            responseObserver.onNext(HelloReply.newBuilder().setMessage("And again Hi " + request.getName()).build());
        }
        responseObserver.onCompleted();
    }

}
