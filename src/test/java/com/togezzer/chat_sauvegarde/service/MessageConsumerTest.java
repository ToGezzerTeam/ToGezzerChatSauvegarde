package com.togezzer.chat_sauvegarde.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.togezzer.chat_sauvegarde.dto.ContentDTO;
import com.togezzer.chat_sauvegarde.dto.MessageDTO;
import com.togezzer.chat_sauvegarde.enums.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

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

        ContentDTO content = new ContentDTO();
        content.setType(ContentType.TEXT);
        content.setValue("Blablabla");

        message = new MessageDTO();
        message.setContent(content);
        message.setAuthorId("uuid1");
        message.setRoomId("2L");
    }

    @Test
    void testMessageSauvegarde() {
        consumer.consumeMessages(message);
        verify(messageService).saveMessage(message);
    }

    @Test
    void testLogMessageRecue() {
        consumer.consumeMessages(message);

        assertThat(listAppender.list)
                .anyMatch(log -> log.getMessage().contains("Message reçu"));
    }

    @Test
    void testLogMessageSauvegardee() {
        consumer.consumeMessages(message);

        assertThat(listAppender.list)
                .anyMatch(log -> log.getMessage().contains("Message sauvegardé"));
    }

    @Test
    void testErreurMongo_relanceException() {
        doThrow(new RuntimeException("Erreur Mongo")).when(messageService).saveMessage(message);

        assertThrows(RuntimeException.class, () -> consumer.consumeMessages(message));

        verify(messageService).saveMessage(message);
    }

    @Test
    void testLogErreurMongo() {
        doThrow(new RuntimeException("Erreur Mongo")).when(messageService).saveMessage(message);

        assertThrows(RuntimeException.class, () -> consumer.consumeMessages(message));

        assertThat(listAppender.list)
                .anyMatch(log -> log.getLevel() == Level.ERROR
                        && log.getMessage().contains("Erreur Mongo lors de la sauvegarde"));
    }
}