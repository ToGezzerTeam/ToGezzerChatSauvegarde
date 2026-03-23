package com.togezzer.chatsauvegarde.config;

import jakarta.validation.ConstraintViolationException;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.Map;

@Configuration
public class RetryConfig {

    @Value("${rabbitmq.retry.max-attempts}")
    private int maxAttempts;

    /**
     * Intercepte les exceptions après maxAttempts :
     * on laisse remonter pour que RabbitMQ route vers la DLQ.
     */
    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(maxAttempts)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .retryPolicy(new SimpleRetryPolicy(maxAttempts,
                        Map.of(
                                DataAccessException.class, true,
                                MessageConversionException.class, false,
                                ConstraintViolationException.class, false
                        ),
                        true
                ))
                .build();
    }
}
