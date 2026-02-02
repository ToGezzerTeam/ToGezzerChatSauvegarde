package com.togezzer.chat_sauvegarde.repository;

import com.togezzer.chat_sauvegarde.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends MongoRepository<MessageEntity, String> {
    @Query("{ 'roomId': ?0, 'createdAt': { $lt: ?1 } }")
    Slice<MessageEntity> findByRoomIdOrderByCreatedAtDesc(String roomId, Instant createdAt, Pageable pageable);
}
