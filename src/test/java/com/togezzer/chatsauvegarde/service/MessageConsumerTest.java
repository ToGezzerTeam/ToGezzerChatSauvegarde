package com.togezzer.chatsauvegarde.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.togezzer.chatsauvegarde.dto.ContentDTO;
import com.togezzer.chatsauvegarde.dto.MessageDTO;
import com.togezzer.chatsauvegarde.enums.ContentType;
import com.togezzer.chatsauvegarde.enums.MessageState;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class MessageConsumerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageConsumer consumer;

    private ListAppender<ILoggingEvent> listAppender;
    private MessageDTO message;

    @BeforeEach
    void setup() {
        Logger logger = (Logger) LoggerFactory.getLogger(MessageConsumer.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);

        ContentDTO content = ContentDTO.builder()
                .type(ContentType.TEXT)
                .value("Blablabla")
                .build();

        message = MessageDTO.builder()
                .content(content)
                .authorId("uuid1")
                .roomId("2")
                .state(MessageState.CREATED)
                .build();
    }

    @Test
    void shouldHandleSaveMessage() {
        consumer.consumeMessages(message);
        verify(messageService).saveMessage(message);
    }

    @Test
    void shouldHandleUpdatedMessage() {
        message.setState(MessageState.UPDATED);
        consumer.consumeMessages(message);
        verify(messageService).updateMessage(message);
    }

    @Test
    void shouldHandleDeletedMessage() {
        message.setState(MessageState.DELETED);
        consumer.consumeMessages(message);
        verify(messageService).deleteMessage(message);
    }

    @Test
    void testLogMessageRecue() {
        consumer.consumeMessages(message);

        assertThat(listAppender.list)
                .anyMatch(log -> log.getMessage().contains("Message received"));
    }

    @Test
    void testLogMessageSauvegardee() {
        consumer.consumeMessages(message);

        assertThat(listAppender.list)
                .anyMatch(log -> log.getMessage().contains("Message saved"));
    }

    @Test
    void testErreurMongo_relanceException() {
        doThrow(new DataIntegrityViolationException("Erreur Mongo")).when(messageService).saveMessage(message);

        assertThrows(DataIntegrityViolationException.class, () -> consumer.consumeMessages(message));

        verify(messageService).saveMessage(message);
    }

    @Test
    void testConstraintViolation_relanceException() {
        doThrow(new ConstraintViolationException("Champ obligatoire manquant", null))
                .when(messageService).saveMessage(message);

        assertThrows(ConstraintViolationException.class, () -> consumer.consumeMessages(message));

        verify(messageService).saveMessage(message);
    }


    @Test
    void testLogErreurMongo() {
        doThrow(new DataIntegrityViolationException("Erreur Mongo"))
                .when(messageService).saveMessage(message);

        assertThrows(DataIntegrityViolationException.class, () -> consumer.consumeMessages(message));

        assertThat(listAppender.list)
                .anyMatch(log -> log.getLevel() == Level.ERROR
                        && log.getMessage().contains("MongoDB error while saving the message"));
    }

    @Test
    void testConstraintViolation_logErreur() {
        doThrow(new ConstraintViolationException("Champ obligatoire manquant", null))
                .when(messageService).saveMessage(message);

        assertThrows(ConstraintViolationException.class, () -> consumer.consumeMessages(message));

        assertThat(listAppender.list)
                .anyMatch(log -> log.getLevel() == Level.ERROR
                        && log.getMessage().contains("Invalid message data"));
    }
}