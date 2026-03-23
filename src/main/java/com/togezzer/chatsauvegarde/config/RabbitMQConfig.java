package com.togezzer.chatsauvegarde.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class RabbitMQConfig implements RabbitListenerConfigurer {

    @Value("${rabbitmq.queues.message}")
    private String messageQueue;

    @Value("${rabbitmq.queues.message-dlq}")
    private String messageDlq;

    @Value("${rabbitmq.exchanges.message}")
    private String messageExchange;

    @Value("${rabbitmq.exchanges.dlq}")
    private String dlqExchange;

    @Value("${rabbitmq.routing-keys.message}")
    private String messageRoutingKey;

    @Value("${rabbitmq.routing-keys.dlq}")
    private String dlqRoutingKey;

    private final LocalValidatorFactoryBean validator;

    public RabbitMQConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(messageDlq).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlqExchange);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(dlqRoutingKey);
    }

    @Bean
    public Queue messageQueue() {
        return QueueBuilder.durable(messageQueue)
                .withArgument("x-dead-letter-exchange", dlqExchange)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange(messageExchange);
    }

    @Bean
    public Binding messageBinding() {
        return BindingBuilder.bind(messageQueue())
                .to(messageExchange())
                .with(messageRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public MessageHandlerMethodFactory messageHandlerMethodFactory() {
        final var factory = new DefaultMessageHandlerMethodFactory();
        factory.setValidator(validator);

        MappingJackson2MessageConverter jacksonConverter = new MappingJackson2MessageConverter();

        factory.setMessageConverter(jacksonConverter);

        return factory;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter,
            @Qualifier("retryInterceptor") RetryOperationsInterceptor retryInterceptor) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setAdviceChain(retryInterceptor);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}