package com.togezzer.chatsauvegarde.service;

import com.togezzer.chatsauvegarde.dto.MessageDTO;
import com.togezzer.chatsauvegarde.enums.MessageState;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataAccessException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);
    private final MessageService messageService;

    @RabbitListener(queues = "${rabbitmq.queues.message}", containerFactory = "rabbitListenerContainerFactory")
    public void consumeMessages(@Payload @Valid MessageDTO message) {
        log.info("Message received : {}", message);
        try {
            handleMessage(message);
            log.info("Message saved and removed from the queue");
        } catch (DataAccessException e) {
            log.error("MongoDB error while saving the message", e);
            throw e;
        } catch (ConstraintViolationException e) {
            log.error("Invalid message data", e);
            throw e;
        }
    }

    private void handleMessage(MessageDTO message) {
        switch (message.getState()){
            case MessageState.CREATED :
                messageService.saveMessage(message);
                break;
            case MessageState.UPDATED :
                messageService.updateMessage(message);
                break;
            case MessageState.DELETED :
                messageService.deleteMessage(message);
                break;
        }
    }
}
