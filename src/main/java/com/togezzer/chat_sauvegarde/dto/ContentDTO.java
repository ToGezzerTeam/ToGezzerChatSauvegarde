package com.togezzer.chat_sauvegarde.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentDTO {

    @NotBlank
    private String type;

    @NotBlank
    private String value;
}
