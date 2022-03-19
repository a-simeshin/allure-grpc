package com.github.allure.extensions.config;

import com.github.allure.extensions.GreeterGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@GrpcClientBean(
        clazz = GreeterGrpc.GreeterBlockingStub.class,
        beanName = "greeterBlockingStub",
        client = @GrpcClient(value = "testing")
)
@Configuration
public class ClientConfig {

    @Bean
    GrpcServerEmulator grpcServerEmulator() {
        return new GrpcServerEmulator();
    }
}
