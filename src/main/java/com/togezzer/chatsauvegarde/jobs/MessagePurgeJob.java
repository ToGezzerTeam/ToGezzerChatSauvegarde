package com.togezzer.chatsauvegarde.jobs;

import com.togezzer.chatsauvegarde.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePurgeJob {

    private final MessageRepository messageRepository;
    private final Clock clock;

    @Scheduled(cron = "0 0 3 * * *")
    public void purgeSoftDeletedMessages() {
        Instant threshold = Instant.now(clock).minus(90, ChronoUnit.DAYS);
        long deleted = messageRepository.deleteByDeletedAtBefore(threshold);
        log.info("Purged soft-deleted messages older than 90 days: {} removed", deleted);
    }
}
