package com.togezzer.chatsauvegarde.repository;

import com.togezzer.chatsauvegarde.entity.MessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface MessageRepository extends MongoRepository<MessageEntity, String> {

    Slice<MessageEntity> findByRoomIdAndDeletedAtIsNullOrderByCreatedAtDesc(String roomId, Pageable pageable);

    @Query("{ 'roomId': ?0,'deletedAt': null, $or: [ " +
            "  { 'createdAt': { $lt: ?1 } }, " +
            "  { 'createdAt': ?1, 'uuid': { $lt: ?2 } } " +
            "] }")
    Slice<MessageEntity> findMessagesBeforeUuid(String roomId, Instant createdAt, String uuid, Pageable pageable);

    @Query(value = "{ 'uuid': ?0, 'roomId': ?1, 'deletedAt': null }", fields = "{ 'createdAt': 1 }")
    Optional<MessageEntity> findCreatedAtByUuidAndRoomId(String uuid,String roomId);

    Optional<MessageEntity> findByUuidAndRoomId(String uuid, String roomId);

    long deleteByDeletedAtBefore(Instant threshold);
}
