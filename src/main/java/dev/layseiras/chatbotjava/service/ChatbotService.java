package dev.layseiras.chatbotjava.service;

import dev.layseiras.chatbotjava.config.GeminiApi;
import dev.layseiras.chatbotjava.dtos.ChatbotRequest;
import dev.layseiras.chatbotjava.dtos.ChatbotResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ChatbotService {

    private final String API_URL;
    private final String API_KEY;

    private WebClient webClient;

    @Autowired
    public ChatbotService(GeminiApi gemini) {
        this.API_KEY = gemini.getApiKey();
        this.API_URL = gemini.getApiUrl() + API_KEY;

        this.webClient = WebClient.builder()
                .baseUrl(API_URL)
                .defaultHeader("content-type", "application/json")
                .build();
    }

    public String processUserInput(ChatbotRequest request) {
        return """
                {
                  "contents": [
                        {
                        "parts": [
                        {
                            "text": "%s"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(request.userInput());
    }
}
