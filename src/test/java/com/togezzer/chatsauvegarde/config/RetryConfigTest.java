package com.togezzer.chatsauvegarde.config;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryOperations;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryConfigTest {

    @Test
    void shouldCreateRetryInterceptorWithConfiguredMaxAttempts() {
        RetryOperationsInterceptor interceptor = newConfig(5).retryInterceptor();
        assertThat(interceptor).isNotNull();
        assertThat(extractRetryOperations(interceptor)).isNotNull();
    }

    @Test
    void shouldRetryForDataAccessExceptions() {
        RetryOperationsInterceptor interceptor = newConfig(3).retryInterceptor();
        RetryOperations retryOps = extractRetryOperations(interceptor);

        final int[] attempts = {0};

        assertThatThrownBy(() ->
                retryOps.execute((RetryCallback<Void, RuntimeException>) ctx -> {
                    attempts[0]++;
                    throw new DataAccessResourceFailureException("mongo down");
                })
        ).isInstanceOf(DataAccessResourceFailureException.class);

        assertThat(attempts[0]).isEqualTo(3);
    }

    @Test
    void shouldNotRetryForConstraintViolationException() {
        RetryOperationsInterceptor interceptor = newConfig(7).retryInterceptor();
        RetryOperations retryOps = extractRetryOperations(interceptor);

        final int[] attempts = {0};
        assertThatThrownBy(() ->
                retryOps.execute((RetryCallback<Void, RuntimeException>) ctx -> {
                    attempts[0]++;
                    throw new ConstraintViolationException("invalid", null);
                })
        ).isInstanceOf(ConstraintViolationException.class);

        assertThat(attempts[0]).isEqualTo(1);
    }

    @Test
    void shouldNotRetryForMessageConversionException() {
        RetryOperationsInterceptor interceptor = newConfig(7).retryInterceptor();
        RetryOperations retryOps = extractRetryOperations(interceptor);

        final int[] attempts = {0};
        assertThatThrownBy(() ->
                retryOps.execute((RetryCallback<Void, RuntimeException>) ctx -> {
                    attempts[0]++;
                    throw new MessageConversionException("bad payload");
                })
        ).isInstanceOf(MessageConversionException.class);

        assertThat(attempts[0]).isEqualTo(1);
    }

    private static RetryConfig newConfig(int maxAttempts) {
        RetryConfig config = new RetryConfig();
        setField(config, maxAttempts);
        return config;
    }

    private static RetryOperations extractRetryOperations(RetryOperationsInterceptor interceptor) {
        try {
            Field f = RetryOperationsInterceptor.class.getDeclaredField("retryOperations");
            f.setAccessible(true);
            return (RetryOperations) f.get(interceptor);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible d'extraire RetryOperations depuis RetryOperationsInterceptor", e);
        }
    }

    private static void setField(Object target, Object value) {
        try {
            Field f = target.getClass().getDeclaredField("maxAttempts");
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible d'injecter le champ " + "maxAttempts", e);
        }
    }
}
