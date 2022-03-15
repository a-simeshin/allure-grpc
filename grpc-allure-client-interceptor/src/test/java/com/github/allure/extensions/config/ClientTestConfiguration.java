package com.github.allure.extensions.config;

import com.github.allure.extensions.AllureGrpcClientInterceptor;
import com.github.allure.extensions.GreeterGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@GrpcClientBean(
        clazz = GreeterGrpc.GreeterBlockingStub.class,
        beanName = "greeterBlockingStub",
        client = @GrpcClient(value = "testing", interceptors = AllureGrpcClientInterceptor.class)
)
public class ClientTestConfiguration {

    @Bean
    GrpcServerEmulator grpcServerEmulator() {
        return new GrpcServerEmulator();
    }
    
}
