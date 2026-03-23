package com.togezzer.chatsauvegarde.config;

import jakarta.validation.ConstraintViolationException;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

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
        // Policy: retry uniquement sur les erreurs techniques (ex: DB).
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(
                maxAttempts,
                Map.of(
                        DataAccessException.class, true,
                        MessageConversionException.class, false,
                        ConstraintViolationException.class, false,
                        ListenerExecutionFailedException.class, false
                ),
                true
        );

        // Backoff: 1s, x2, max 10s
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return RetryInterceptorBuilder.stateless()
                .retryOperations(retryTemplate)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }
}
