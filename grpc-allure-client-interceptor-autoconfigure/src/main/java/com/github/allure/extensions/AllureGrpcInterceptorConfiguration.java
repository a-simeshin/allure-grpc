package com.github.allure.extensions;

import net.devh.boot.grpc.client.interceptor.GlobalClientInterceptorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Editing interceptor list in GlobalClientInterceptorConfigurer with this autoconfiguration
 */
@Configuration
public class AllureGrpcInterceptorConfiguration {

    @Bean
    @Primary
    GlobalClientInterceptorConfigurer globalClientInterceptorConfigurer() {
        return interceptors -> interceptors.add(new AllureGrpcClientInterceptor());
    }
}
