package com.togezzer.chatsauvegarde.service;

import com.togezzer.chatsauvegarde.dto.MessageDTO;
import com.togezzer.chatsauvegarde.dto.MessagesPageResponseDto;
import com.togezzer.chatsauvegarde.entity.MessageEntity;
import com.togezzer.chatsauvegarde.exception.MessageUuidNotFoundException;
import com.togezzer.chatsauvegarde.mapper.MessageMapper;
import com.togezzer.chatsauvegarde.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    public void updateMessage(MessageDTO messageDTO) {
        upsertFromDto(messageDTO);
    }

    public void deleteMessage(MessageDTO messageDTO) {
        upsertFromDto(messageDTO);
    }

    private void upsertFromDto(MessageDTO messageDTO) {
        MessageEntity messageEntity = getMessageOrThrow(messageDTO.getUuid(), messageDTO.getRoomId());
        messageMapper.updateEntityFromDto(messageDTO, messageEntity);
        messageRepository.save(messageEntity);
    }

    private MessageEntity getMessageOrThrow(String uuid, String roomId) {
        return messageRepository.findByUuidAndRoomId(uuid, roomId)
                .orElseThrow(() -> new MessageUuidNotFoundException(uuid, roomId));
    }

    public MessagesPageResponseDto getMessages(String roomId, String messageUuid, int pageSize){
        log.debug("Fetching messages for room: {}, messageUuid: {}, pageSize: {}", roomId, messageUuid != null ? messageUuid : "initial load", pageSize);

        final Slice<MessageEntity> messageEntities;

        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(MessageEntity.Fields.createdAt).descending());

        if(messageUuid != null && !messageUuid.isEmpty()){
            MessageEntity referenceMessage = messageRepository.findCreatedAtByUuidAndRoomId(messageUuid,roomId)
                    .orElseThrow(() -> new MessageUuidNotFoundException(messageUuid,roomId));

            messageEntities = messageRepository.findMessagesBeforeUuid(roomId,referenceMessage.getCreatedAt(),messageUuid, pageable);
        }else{
            messageEntities = messageRepository.findByRoomIdAndDeletedAtIsNullOrderByCreatedAtDesc(roomId, pageable);
        }

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

    public MessageDTO getMessageByUuidAndRoomId(String roomId, String messageUuid) {
        MessageEntity messageEntity = getMessageOrThrow(messageUuid, roomId);
        return messageMapper.toDto(messageEntity);
    }
}
