package com.togezzer.chatsauvegarde.dto;

import com.togezzer.chatsauvegarde.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentDTO {

    @NotNull
    private ContentType type;

    @NotBlank
    private String value;
}
