package dev.layseiras.chatbotjava.controller;

import dev.layseiras.chatbotjava.dtos.*;
import dev.layseiras.chatbotjava.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatbotController {

    private final ChatbotService service;

    public ChatbotController(ChatbotService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ChatbotResponse> interact(@RequestBody ChatbotRequest request) {
        try {
            ChatbotResponse response = this.service.interactWithChat(request.userInput());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
