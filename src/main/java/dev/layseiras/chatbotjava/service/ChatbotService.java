package dev.layseiras.chatbotjava.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                            "text": "Você é um agente de suporte da loja canjiquinha e ela é do ramo de tecnologia, vendendo periféricos etc. Sua função é ajudar a sanar os questionamentos dos clientes. Questionamento: %s"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(request.userInput());
    }

    public Mono<String> getChatbotOutput(ChatbotRequest request) {
        String requestBody = processUserInput(request);

        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(response);
                        JsonNode candidates = jsonNode.path("candidates");

                        if (candidates.isArray() && candidates.size() > 0) {
                            JsonNode content = candidates.get(0).path("content");
                            if (content.has("parts")) {
                                return content.get("parts").get(0).path("text").asText();
                            }
                        }
                        return "Não foi possível gerar uma resposta do chat.";

                    } catch (Exception e) {
                        e.printStackTrace();
                        return "Erro ao processar a resposta da API: " + e.getMessage();
                    }
                });
    }
}
