package com.togezzer.chat_sauvegarde.service;

import com.togezzer.chat_sauvegarde.dto.MessageDTO;
import com.togezzer.chat_sauvegarde.dto.MessagesPageResponseDto;
import com.togezzer.chat_sauvegarde.entity.MessageEntity;
import com.togezzer.chat_sauvegarde.mapper.MessageMapper;
import com.togezzer.chat_sauvegarde.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;
    private final MessageRepository messageRepository;
    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    public void saveMessage(MessageDTO messageDTO){
        MessageEntity messageEntity = messageMapper.toEntity(messageDTO);
        messageRepository.save(messageEntity);
    }

    public MessagesPageResponseDto getMessages(String roomId, Instant date, int pageSize){
        log.debug("Fetching messages for room: {}, from date: {}, pageSize: {}", roomId, date, pageSize);

        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(MessageEntity.Fields.createdAt).descending());
        Slice<MessageEntity> messageEntities = messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId,date, pageable);

        log.debug("Found {} messages in room {}", messageEntities.getContent().size(), roomId);

        List<MessageDTO> messageDTOS = messageEntities
                .stream()
                .map(messageMapper::toDto)
                .toList()
                .reversed();

        return new MessagesPageResponseDto(
                messageDTOS,
                messageEntities.hasNext()
        );
    }
}
