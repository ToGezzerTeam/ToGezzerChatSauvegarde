package com.togezzer.chatsauvegarde.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorResponseField {
    ERROR("error"),
    MESSAGE("message");

    private final String fieldName;
}