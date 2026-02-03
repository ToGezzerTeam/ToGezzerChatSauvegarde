package com.togezzer.chatsauvegarde.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;


public record MessagesPageResponseDto(
        @NotNull
        List<MessageDTO> messageDTOS,

        @NotNull
        boolean hasMore
){}
