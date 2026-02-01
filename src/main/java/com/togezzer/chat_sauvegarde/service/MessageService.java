package com.togezzer.chat_sauvegarde.service;

import com.togezzer.chat_sauvegarde.dto.MessageDTO;
import com.togezzer.chat_sauvegarde.entity.MessageEntity;
import com.togezzer.chat_sauvegarde.mapper.MessageMapper;
import com.togezzer.chat_sauvegarde.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;

    public void saveMessage(MessageDTO messageDTO){
        MessageEntity messageEntity = messageMapper.toEntity(messageDTO);
        messageRepository.save(messageEntity);
    }
}
