package com.togezzer.chatSauvegarde.controller;

import com.togezzer.chatSauvegarde.dto.MessagesPageResponseDto;
import com.togezzer.chatSauvegarde.service.MessageService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor

@Validated
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/{roomId}")
    public MessagesPageResponseDto getMessages(@PathVariable @NotBlank String roomId,
                                               @RequestParam(required = false) String messageUuid,
                                               @RequestParam(defaultValue = "100") @Min(1) int pageSize){
        return messageService.getMessages(roomId,messageUuid,pageSize);
    }
}
