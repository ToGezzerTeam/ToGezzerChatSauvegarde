package com.togezzer.chat_sauvegarde.entity;

import com.togezzer.chat_sauvegarde.enums.ContentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ContentEntity {
    private ContentType type;
    private String value;
}
