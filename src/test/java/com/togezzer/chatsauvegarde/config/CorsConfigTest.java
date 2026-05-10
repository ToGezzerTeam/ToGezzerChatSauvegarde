package com.togezzer.chatsauvegarde.config;

import com.togezzer.chatsauvegarde.controller.MessageController;
import com.togezzer.chatsauvegarde.dto.MessageDTO;
import com.togezzer.chatsauvegarde.dto.MessagesPageResponseDto;
import com.togezzer.chatsauvegarde.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.springframework.http.HttpHeaders.ORIGIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@Import(CorsConfig.class)
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    void shouldExposeCorsHeadersForPreflightRequests() throws Exception {
        mockMvc.perform(options("/api/messages/{roomId}", "general")
                        .header(ORIGIN, "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")));
    }

    @Test
    void shouldExposeCorsHeadersForActualRequests() throws Exception {
        String roomId = "general";
        List<MessageDTO> messages = List.of(MessageDTO.builder().build());
        when(messageService.getMessages(eq(roomId), isNull(), eq(100)))
                .thenReturn(new MessagesPageResponseDto(messages, true));

        mockMvc.perform(get("/api/messages/{roomId}", roomId)
                        .header(ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string(ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }
}

