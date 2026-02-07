package com.togezzer.chatsauvegarde.service;

import com.togezzer.chatsauvegarde.dto.ContentDTO;
import com.togezzer.chatsauvegarde.dto.MessageDTO;
import com.togezzer.chatsauvegarde.dto.MessagesPageResponseDto;
import com.togezzer.chatsauvegarde.entity.ContentEntity;
import com.togezzer.chatsauvegarde.entity.MessageEntity;
import com.togezzer.chatsauvegarde.enums.ContentType;
import com.togezzer.chatsauvegarde.exception.MessageUuidNotFoundException;
import com.togezzer.chatsauvegarde.mapper.MessageMapper;
import com.togezzer.chatsauvegarde.repository.MessageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
        MessageDTO dto = MessageDTO.builder().build();
        MessageEntity entity = MessageEntity.builder().build();

        when(messageMapper.toEntity(dto)).thenReturn(entity);

        messageService.saveMessage(dto);

        verify(messageMapper, times(1)).toEntity(dto);
        verify(messageRepository, times(1)).save(entity);
    }

    @Test
    void saveMessage_repositoryThrowsException_shouldPropagate() {
        MessageDTO dto = MessageDTO.builder().build();
        MessageEntity entity = MessageEntity.builder().build();

        when(messageMapper.toEntity(dto)).thenReturn(entity);
        when(messageRepository.save(entity)).thenThrow(new RuntimeException("Mongo error"));

        Assertions.assertThrows(RuntimeException.class, () -> messageService.saveMessage(dto));
    }

    @Test
    void getMessages_should_throw_MessageUuidNotFoundException_when_messageUuid_not_in_roomId(){
        String roomId = "roomId";
        String messageUuid = "messageUuid";

        when(messageRepository.findCreatedAtByUuidAndRoomId(messageUuid,roomId)).thenReturn(Optional.empty());

        Assertions.assertThrows(MessageUuidNotFoundException.class, ()-> messageService.getMessages(roomId,messageUuid,10));
    }

    @Test
    void getMessage_should_call_findByRoomIdOrderByCreatedAtDesc_when_messageUuid_is_empty(){
        String roomId = "roomId";
        String messageUuid = "";
        int pageSize = 10;

        Pageable expectedPageable = PageRequest.of(0, pageSize, Sort.by(MessageEntity.Fields.createdAt).descending());
        Slice<MessageEntity> messageSlice = new SliceImpl<>(List.of(), expectedPageable, false);
        when(messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, expectedPageable))
                .thenReturn(messageSlice);


        messageService.getMessages(roomId, messageUuid, pageSize);


        verify(messageRepository).findByRoomIdOrderByCreatedAtDesc(roomId, expectedPageable);
    }

    @Test
    void getMessage_should_call_findMessagesBeforeUuid_when_messageUuid_is_not_empty(){
        String roomId = "roomId";
        String messageUuid = "messageUuid";
        int pageSize = 10;

        MessageEntity messageEntity = MessageEntity.builder()
                .createdAt(Instant.now())
                .build();

        Pageable expectedPageable = PageRequest.of(0, pageSize, Sort.by(MessageEntity.Fields.createdAt).descending());
        Slice<MessageEntity> messageSlice = new SliceImpl<>(List.of(), expectedPageable, false);

        when(messageRepository.findCreatedAtByUuidAndRoomId(messageUuid,roomId)).thenReturn(Optional.of(messageEntity));
        when(messageRepository.findMessagesBeforeUuid(roomId,messageEntity.getCreatedAt(),messageUuid, expectedPageable))
                .thenReturn(messageSlice);


        messageService.getMessages(roomId, messageUuid, pageSize);


        verify(messageRepository).findMessagesBeforeUuid(roomId,messageEntity.getCreatedAt(),messageUuid, expectedPageable);
    }

    @Test
    void getMessage_should_return_MessagesPageResponseDto_in_order_and_hasNextFalse(){
        String roomId = "roomId";
        String messageUuid = "messageUuid";
        ContentType type = ContentType.TEXT;
        String contentValue = "blabla";
        String authorId = "authorId";
        String answerTo = "answerTo";
        Instant createdAt = Instant.now();
        int pageSize = 10;

        MessageEntity messageEntity1 = createMessageEntity(messageUuid,roomId,type,contentValue,createdAt,authorId,answerTo);
        MessageEntity messageEntity2 = createMessageEntity(messageUuid,roomId,type,contentValue,createdAt,authorId,answerTo);

        MessageDTO messageDTO1 = createMessageDTO(messageUuid,roomId,type,contentValue,createdAt,authorId,answerTo);
        MessageDTO messageDTO2 = createMessageDTO(messageUuid,roomId,type,contentValue,createdAt,authorId,answerTo);

        Pageable expectedPageable = PageRequest.of(0, pageSize, Sort.by(MessageEntity.Fields.createdAt).descending());
        Slice<MessageEntity> messageSlice = new SliceImpl<>(List.of(messageEntity1,messageEntity2), expectedPageable, false);
        when(messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, expectedPageable))
                .thenReturn(messageSlice);

        when(messageMapper.toDto(messageEntity1)).thenReturn(messageDTO1);
        when(messageMapper.toDto(messageEntity2)).thenReturn(messageDTO2);

        MessagesPageResponseDto messagesPageResponseDto = messageService.getMessages(roomId, "", pageSize);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2,messagesPageResponseDto.messageDTOS().size()),
                () -> Assertions.assertEquals(messageDTO2, messagesPageResponseDto.messageDTOS().getFirst()),
                () -> Assertions.assertEquals(messageDTO1, messagesPageResponseDto.messageDTOS().get(1)),
                () -> Assertions.assertFalse(messagesPageResponseDto.hasMore())
        );
    }

    @Test
    void getMessage_should_return_MessagesPageResponseDto_in_order_and_hasNextTrue(){
        String roomId = "roomId";
        String messageUuid = "messageUuid";
        ContentType type = ContentType.TEXT;
        String contentValue = "blabla";
        String authorId = "authorId";
        String answerTo = "answerTo";
        Instant createdAt = Instant.now();
        int pageSize = 10;

        MessageEntity messageEntity1 = createMessageEntity(messageUuid,roomId,type,contentValue,createdAt,authorId,answerTo);
        MessageEntity messageEntity2 = createMessageEntity(messageUuid,roomId,type,contentValue,createdAt,authorId,answerTo);

        MessageDTO messageDTO1 = createMessageDTO(messageUuid,roomId,type,contentValue,createdAt,authorId,answerTo);
        MessageDTO messageDTO2 = createMessageDTO(messageUuid,roomId,type,contentValue,createdAt,authorId,answerTo);

        Pageable expectedPageable = PageRequest.of(0, pageSize, Sort.by(MessageEntity.Fields.createdAt).descending());
        Slice<MessageEntity> messageSlice = new SliceImpl<>(List.of(messageEntity1,messageEntity2), expectedPageable, true);
        when(messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, expectedPageable))
                .thenReturn(messageSlice);

        when(messageMapper.toDto(messageEntity1)).thenReturn(messageDTO1);
        when(messageMapper.toDto(messageEntity2)).thenReturn(messageDTO2);

        MessagesPageResponseDto messagesPageResponseDto = messageService.getMessages(roomId, "", pageSize);

        Assertions.assertAll(
                () -> Assertions.assertEquals(2,messagesPageResponseDto.messageDTOS().size()),
                () -> Assertions.assertEquals(messageDTO2, messagesPageResponseDto.messageDTOS().getFirst()),
                () -> Assertions.assertEquals(messageDTO1, messagesPageResponseDto.messageDTOS().get(1)),
                () -> Assertions.assertTrue(messagesPageResponseDto.hasMore())
        );
    }

    private MessageEntity createMessageEntity(String uuid,String roomId, ContentType type, String contentValue,
                                                      Instant createdAt, String authorId, String answerTo) {
        ContentEntity contentEntity = ContentEntity.builder()
                .type(type)
                .value(contentValue)
                .build();

        return MessageEntity.builder()
                .uuid(uuid)
                .roomId(roomId)
                .content(contentEntity)
                .createdAt(createdAt)
                .authorId(authorId)
                .answerTo(answerTo)
                .build();
    }

    private MessageDTO createMessageDTO(String uuid,String roomId, ContentType type, String contentValue,
                                        Instant createdAt, String authorId, String answerTo) {
        ContentDTO contentDTO = ContentDTO.builder()
                .type(type)
                .value(contentValue)
                .build();

        return MessageDTO.builder()
                .uuid(uuid)
                .roomId(roomId)
                .content(contentDTO)
                .createdAt(createdAt)
                .authorId(authorId)
                .answerTo(answerTo)
                .build();
    }

}
