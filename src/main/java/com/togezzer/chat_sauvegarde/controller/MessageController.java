package com.togezzer.chat_sauvegarde.controller;

import com.togezzer.chat_sauvegarde.dto.MessagesPageResponseDto;
import com.togezzer.chat_sauvegarde.service.MessageService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor

@Validated
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/{roomId}")
    public MessagesPageResponseDto getMessages(@PathVariable String roomId,
                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant date,
                                               @RequestParam(defaultValue = "100") @Min(1) int pageSize){
        date = date != null ? date : Instant.now();
        return messageService.getMessages(roomId,date,pageSize);
    }
}
