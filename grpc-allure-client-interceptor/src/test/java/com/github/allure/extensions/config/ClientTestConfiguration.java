package com.github.allure.extensions.config;

import com.github.allure.extensions.AllureGrpcClientInterceptor;
import com.github.allure.extensions.GreeterGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@GrpcClientBean(
        clazz = GreeterGrpc.GreeterBlockingStub.class,
        beanName = "greeterBlockingStub",
        client = @GrpcClient(value = "testing")
)
public class ClientTestConfiguration {

    @Bean
    GrpcServerEmulator grpcServerEmulator() {
        return new GrpcServerEmulator();
    }

    @Bean
    @Primary
    GlobalClientInterceptorConfigurer globalClientInterceptorConfigurer() {
        return interceptors -> interceptors.add(new AllureGrpcClientInterceptor());
    }

}
