package com.togezzer.chat_sauvegarde.service;

import com.togezzer.chat_sauvegarde.dto.MessageDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);
    private final MessageService messageService;

    @RabbitListener(queues = "queue-message")
    public void consumeMessages(@Payload @Valid MessageDTO message) {
        log.info("Message reçu : {}", message);
        try {
            messageService.saveMessage(message);
            log.info("Message sauvegardé et retiré de la queue.");
        } catch (Exception e) {
            log.error("Erreur Mongo lors de la sauvegarde : {}", e.getMessage());
            throw e;
        }
    }
}
