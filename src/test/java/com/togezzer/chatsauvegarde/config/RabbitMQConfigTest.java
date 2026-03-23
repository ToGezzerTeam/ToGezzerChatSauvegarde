package com.togezzer.chatsauvegarde.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {

    @Test
    void shouldCreateMainQueueWithDeadLetterArguments() {
        RabbitMQConfig config = newConfig();

        Queue mainQueue = config.messageQueue();

        assertThat(mainQueue.getName()).isEqualTo("queue-message");
        assertThat(mainQueue.getArguments()).containsEntry("x-dead-letter-exchange", "exchange-message.dlq");
        assertThat(mainQueue.getArguments()).containsEntry("x-dead-letter-routing-key", "routing-message.dlq");
    }

    @Test
    void shouldCreateDlqQueue() {
        RabbitMQConfig config = newConfig();

        Queue dlq = config.deadLetterQueue();

        assertThat(dlq.getName()).isEqualTo("queue-message.dlq");
    }

    @Test
    void shouldCreateExchanges() {
        RabbitMQConfig config = newConfig();

        DirectExchange mainExchange = config.messageExchange();
        DirectExchange dlxExchange = config.deadLetterExchange();

        assertThat(mainExchange.getName()).isEqualTo("exchange-message");
        assertThat(dlxExchange.getName()).isEqualTo("exchange-message.dlq");
    }

    @Test
    void shouldCreateBindings() {
        RabbitMQConfig config = newConfig();

        Binding mainBinding = config.messageBinding();
        Binding dlqBinding = config.deadLetterBinding();

        assertThat(mainBinding.getRoutingKey()).isEqualTo("routing-message");
        assertThat(dlqBinding.getRoutingKey()).isEqualTo("routing-message.dlq");
    }

    private static RabbitMQConfig newConfig() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        RabbitMQConfig config = new RabbitMQConfig(validator);

        // Injecte les @Value via reflection pour éviter de lancer Spring.
        setField(config, "messageQueue", "queue-message");
        setField(config, "messageDlq", "queue-message.dlq");
        setField(config, "messageExchange", "exchange-message");
        setField(config, "dlqExchange", "exchange-message.dlq");
        setField(config, "messageRoutingKey", "routing-message");
        setField(config, "dlqRoutingKey", "routing-message.dlq");

        return config;
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Impossible d'injecter le champ " + fieldName, e);
        }
    }
}
