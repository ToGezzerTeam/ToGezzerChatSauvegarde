package com.togezzer.chatsauvegarde.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@FieldNameConstants(onlyExplicitlyIncluded = true)
@Document(collection = "messages")
public class MessageEntity {
    @Id
    private String uuid;

    private String roomId;
    private String authorId;
    private String answerTo;
    private ContentEntity content;

    @FieldNameConstants.Include
    private Instant createdAt;
}
