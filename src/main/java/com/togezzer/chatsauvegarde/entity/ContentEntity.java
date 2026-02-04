package com.togezzer.chatsauvegarde.entity;

import com.togezzer.chatsauvegarde.enums.ContentType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ContentEntity {
    private ContentType type;
    private String value;
}
