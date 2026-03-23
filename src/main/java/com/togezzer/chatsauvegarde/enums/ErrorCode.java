package com.togezzer.chatsauvegarde.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    VALIDATION_FAILED("Validation Failed", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT("Invalid Format", HttpStatus.BAD_REQUEST),
    MESSAGE_NOT_FOUND("Message Not Found", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus status;
}