package com.togezzer.chat_sauvegarde.service;

import com.togezzer.chat_sauvegarde.dto.MessageDTO;
import com.togezzer.chat_sauvegarde.entity.MessageEntity;
import com.togezzer.chat_sauvegarde.mapper.MessageMapper;
import com.togezzer.chat_sauvegarde.repository.MessageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {
    @Mock
    private MessageMapper messageMapper;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    void saveMessage_callsMapperAndRepository() {
        MessageDTO dto = new MessageDTO();
        MessageEntity entity = new MessageEntity();

        when(messageMapper.toEntity(dto)).thenReturn(entity);

        messageService.saveMessage(dto);

        verify(messageMapper, times(1)).toEntity(dto);
        verify(messageRepository, times(1)).save(entity);
    }

    @Test
    void saveMessage_repositoryThrowsException_shouldPropagate() {
        MessageDTO dto = new MessageDTO();
        MessageEntity entity = new MessageEntity();

        when(messageMapper.toEntity(dto)).thenReturn(entity);
        when(messageRepository.save(entity)).thenThrow(new RuntimeException("Mongo error"));

        Assertions.assertThrows(RuntimeException.class, () -> {
            messageService.saveMessage(dto);
        });
    }
}
