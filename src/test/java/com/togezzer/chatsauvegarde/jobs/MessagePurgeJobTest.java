package com.togezzer.chatsauvegarde.jobs;

import com.togezzer.chatsauvegarde.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessagePurgeJobTest {

    @Mock
    private MessageRepository messageRepository;

    @Test
    void purgeSoftDeletedMessages_should_delete_before_threshold_90_days() {
        Instant now = Instant.parse("2026-01-10T03:00:00Z");
        Clock fixedClock = Clock.fixed(now, ZoneOffset.UTC);

        when(messageRepository.deleteByDeletedAtBefore(any(Instant.class))).thenReturn(7L);

        MessagePurgeJob job = new MessagePurgeJob(messageRepository, fixedClock);

        job.purgeSoftDeletedMessages();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(messageRepository, times(1)).deleteByDeletedAtBefore(captor.capture());

        Instant expectedThreshold = now.minusSeconds(90L * 24 * 60 * 60);
        assertEquals(expectedThreshold, captor.getValue());

        verifyNoMoreInteractions(messageRepository);
    }
}