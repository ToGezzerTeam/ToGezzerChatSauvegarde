package com.togezzer.chatSauvegarde.entity;

import com.togezzer.chatSauvegarde.enums.ContentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ContentEntity {
    private ContentType type;
    private String value;
}
