package com.togezzer.chat_sauvegarde.repository;

import com.togezzer.chat_sauvegarde.entity.MessageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<MessageEntity, String> {
}
