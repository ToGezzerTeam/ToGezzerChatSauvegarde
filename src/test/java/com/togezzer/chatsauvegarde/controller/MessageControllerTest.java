package com.togezzer.chatsauvegarde.controller;

import com.togezzer.chatsauvegarde.dto.MessageDTO;
import com.togezzer.chatsauvegarde.dto.MessagesPageResponseDto;
import com.togezzer.chatsauvegarde.enums.ErrorCode;
import com.togezzer.chatsauvegarde.exception.MessageUuidNotFoundException;
import com.togezzer.chatsauvegarde.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
public class MessageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    void shouldReturnMessagesWithDefaultParameters() throws Exception {
        String roomId = "room123";
        List<MessageDTO> messages = List.of(MessageDTO.builder().build());
        MessagesPageResponseDto response = new MessagesPageResponseDto(messages,true);
        when(messageService.getMessages(eq(roomId), isNull(), eq(100)))
                .thenReturn(response);

        mockMvc.perform(get("/api/messages/{roomId}", roomId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        verify(messageService).getMessages(roomId, null, 100);
    }

    @Test
    void shouldReturnMessagesWithMessageUuid() throws Exception {
        String roomId = "room123";
        String messageUuid = "msg-uuid-456";
        List<MessageDTO> messages = List.of(MessageDTO.builder().build());
        MessagesPageResponseDto response = new MessagesPageResponseDto(messages,true);
        when(messageService.getMessages(roomId, messageUuid, 100))
                .thenReturn(response);

        mockMvc.perform(get("/api/messages/{roomId}", roomId)
                        .param("messageUuid", messageUuid))
                .andExpect(status().isOk());

        verify(messageService).getMessages(roomId, messageUuid, 100);
    }

    @Test
    void shouldReturnMessagesWithCustomPageSize() throws Exception {
        String roomId = "room123";
        int pageSize = 50;
        List<MessageDTO> messages = List.of(MessageDTO.builder().build());
        MessagesPageResponseDto response = new MessagesPageResponseDto(messages,true);
        when(messageService.getMessages(roomId, null, pageSize))
                .thenReturn(response);

        mockMvc.perform(get("/api/messages/{roomId}", roomId)
                        .param("pageSize", String.valueOf(pageSize)))
                .andExpect(status().isOk());

        verify(messageService).getMessages(roomId, null, pageSize);
    }

    @Test
    void shouldReturnMessagesWithAllParameters() throws Exception {
        String roomId = "room123";
        String messageUuid = "msg-uuid-456";
        int pageSize = 25;
        List<MessageDTO> messages = List.of(MessageDTO.builder().build());
        MessagesPageResponseDto response = new MessagesPageResponseDto(messages,true);
        when(messageService.getMessages(roomId, messageUuid, pageSize))
                .thenReturn(response);

        mockMvc.perform(get("/api/messages/{roomId}", roomId)
                        .param("messageUuid", messageUuid)
                        .param("pageSize", String.valueOf(pageSize)))
                .andExpect(status().isOk());

        verify(messageService).getMessages(roomId, messageUuid, pageSize);
    }

    @Test
    void shouldReturn400WhenRoomIdIsBlank() throws Exception {
        mockMvc.perform(get("/api/messages/{roomId}", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(ErrorCode.VALIDATION_FAILED.getMessage()))
                .andExpect(jsonPath("$.message").value(containsString("roomId")));

        verify(messageService, never()).getMessages(anyString(), anyString(), anyInt());
    }

    @Test
    void shouldReturn400WhenPageSizeIsZero() throws Exception {
        mockMvc.perform(get("/api/messages/{roomId}", "room123")
                        .param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(ErrorCode.VALIDATION_FAILED.getMessage()))
                .andExpect(jsonPath("$.message").value(containsString("pageSize")));

        verify(messageService, never()).getMessages(anyString(), anyString(), anyInt());
    }

    @Test
    void shouldReturn400WhenPageSizeIsNegative() throws Exception {
        mockMvc.perform(get("/api/messages/{roomId}", "room123")
                        .param("pageSize", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(ErrorCode.VALIDATION_FAILED.getMessage()))
                .andExpect(jsonPath("$.message").value(containsString("pageSize")));

        verify(messageService, never()).getMessages(anyString(), anyString(), anyInt());
    }

    @Test
    void shouldReturn400WhenPageSizeIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/messages/{roomId}", "room123")
                        .param("pageSize", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(ErrorCode.INVALID_FORMAT.getMessage()))
                .andExpect(jsonPath("$.message").value(containsString("pageSize")));

        verify(messageService, never()).getMessages(anyString(), anyString(), anyInt());
    }

    @Test
    void shouldReturn404WhenMessageUuidNotFound() throws Exception {
        String roomId = "room123";
        String messageUuid = "non-existent-uuid";
        when(messageService.getMessages(roomId, messageUuid, 100))
                .thenThrow(new MessageUuidNotFoundException(messageUuid,roomId));

        mockMvc.perform(get("/api/messages/{roomId}", roomId)
                        .param("messageUuid", messageUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(ErrorCode.MESSAGE_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.message").value(containsString(messageUuid)));
    }

    @Test
    void shouldReturn500OnDatabaseError() throws Exception {
        String roomId = "room123";
        when(messageService.getMessages(eq(roomId), isNull(), eq(100)))
                .thenThrow(new DataAccessException("Connection timeout") {});

        mockMvc.perform(get("/api/messages/{roomId}", roomId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.message").value("A technical error occurred. Please try again later."));
    }

    @Test
    void shouldReturn500OnUnexpectedException() throws Exception {
        String roomId = "room123";
        when(messageService.getMessages(eq(roomId), isNull(), eq(100)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/messages/{roomId}", roomId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void shouldAcceptMinimumValidPageSize() throws Exception {
        String roomId = "room123";
        List<MessageDTO> messages = List.of(MessageDTO.builder().build());
        MessagesPageResponseDto response = new MessagesPageResponseDto(messages,true);
        when(messageService.getMessages(roomId, null, 1))
                .thenReturn(response);

        mockMvc.perform(get("/api/messages/{roomId}", roomId)
                        .param("pageSize", "1"))
                .andExpect(status().isOk());

        verify(messageService).getMessages(roomId, null, 1);
    }

    @Test
    void shouldAcceptLargePageSize() throws Exception {
        String roomId = "room123";
        int largePageSize = 10000;
        List<MessageDTO> messages = List.of(MessageDTO.builder().build());
        MessagesPageResponseDto response = new MessagesPageResponseDto(messages,true);
        when(messageService.getMessages(roomId, null, largePageSize))
                .thenReturn(response);

        mockMvc.perform(get("/api/messages/{roomId}", roomId)
                        .param("pageSize", String.valueOf(largePageSize)))
                .andExpect(status().isOk());

        verify(messageService).getMessages(roomId, null, largePageSize);
    }

    @Test
    void shouldAcceptRoomIdWithSpecialCharacters() throws Exception {
        String roomId = "room-123_abc@xyz";
        List<MessageDTO> messages = List.of(MessageDTO.builder().build());
        MessagesPageResponseDto response = new MessagesPageResponseDto(messages,true);
        when(messageService.getMessages(roomId, null, 100))
                .thenReturn(response);

        mockMvc.perform(get("/api/messages/{roomId}", roomId))
                .andExpect(status().isOk());

        verify(messageService).getMessages(roomId, null, 100);
    }

    @Test
    void shouldAcceptEmptyMessageUuid() throws Exception {
        String roomId = "room123";
        List<MessageDTO> messages = List.of(MessageDTO.builder().build());
        MessagesPageResponseDto response = new MessagesPageResponseDto(messages,true);
        when(messageService.getMessages(eq(roomId), eq(""), eq(100)))
                .thenReturn(response);

        mockMvc.perform(get("/api/messages/{roomId}", roomId)
                        .param("messageUuid", ""))
                .andExpect(status().isOk());

        verify(messageService).getMessages(roomId, "", 100);
    }
}

