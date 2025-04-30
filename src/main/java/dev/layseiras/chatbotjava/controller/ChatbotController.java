package dev.layseiras.chatbotjava.controller;

import dev.layseiras.chatbotjava.dtos.*;
import dev.layseiras.chatbotjava.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotService service;

    public ChatbotController(ChatbotService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<String> generateEmail(@RequestBody ChatbotRequest request) {
        return service.getChatbotOutput(request);
    }
}
