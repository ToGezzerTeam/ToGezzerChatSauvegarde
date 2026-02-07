package com.togezzer.chatsauvegarde.exception;

public class MessageUuidNotFoundException extends RuntimeException{
    public MessageUuidNotFoundException(String messageUuid,String roomId){
        super("Message with uuid %s not found in roomId %s".formatted(messageUuid,roomId));
    }
}
