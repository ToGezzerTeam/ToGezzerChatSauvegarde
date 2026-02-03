package com.togezzer.chatSauvegarde.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter

public class MessageDTO {

    private String uuid;

    @NotBlank
    private String roomId;

    @NotBlank
    private String authorId;

    private String answerTo;

    @NotNull
    @Valid
    private ContentDTO content;

    @NotNull
    private Instant createdAt;
}
