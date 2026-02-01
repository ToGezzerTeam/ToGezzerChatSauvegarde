package com.togezzer.chat_sauvegarde.config;

import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class RabbitMQConfig implements RabbitListenerConfigurer {

    private final LocalValidatorFactoryBean validator;

    public RabbitMQConfig(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

    // Convertisseur pour RabbitMQ (Requis pour l'infrastructure)
    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Création de la factory qui va résoudre les arguments de ta méthode (@Payload)
    @Bean
    public MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setValidator(validator);

        // On crée un convertisseur Jackson pour le mapping des méthodes
        MappingJackson2MessageConverter jacksonConverter = new MappingJackson2MessageConverter();
        // On demande à la factory d'utiliser ce convertisseur
        factory.setMessageConverter(jacksonConverter);

        return factory;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        // On lie la factory configurée au registrar de Rabbit
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }
}