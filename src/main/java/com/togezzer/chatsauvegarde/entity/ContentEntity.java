package com.togezzer.chatsauvegarde.entity;

import com.togezzer.chatsauvegarde.enums.ContentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ContentEntity {
    private ContentType type;
    private String value;
}
