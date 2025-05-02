package dev.layseiras.chatbotjava.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.layseiras.chatbotjava.config.GeminiApi;
import dev.layseiras.chatbotjava.dtos.ChatbotRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ChatbotService {

    private final String API_URL;
    private final String API_KEY;

    private WebClient webClient;

    private final ReadFileService fileService;

    @Autowired
    public ChatbotService(GeminiApi gemini, ReadFileService fileService) {
        this.API_KEY = gemini.getApiKey();
        this.API_URL = gemini.getApiUrl() + API_KEY;
        this.fileService = fileService;

        this.webClient = WebClient.builder()
                .baseUrl(API_URL)
                .defaultHeader("content-type", "application/json")
                .build();
    }

    public String processUserInput(ChatbotRequest request) {
        StringBuilder contentBuilder = new StringBuilder();

        contentBuilder.append("""
    Você é um agente virtual da loja Layseiras Shop, especializada em produtos de tecnologia e periféricos gamer. Sua função é atender os clientes com simpatia, clareza e objetividade, utilizando as informações da loja a seguir. Responda perguntas sobre produtos, prazos, políticas de frete e formas de pagamento com base nesses dados. Se não souber algo, oriente o cliente a entrar em contato pelo e-mail oficial ou WhatsApp da loja.
                                                                                                                                                                     :
    %s
    """.formatted(fileService.getTextFile()));


        for (String pastMessage : request.context()) {
            contentBuilder.append("\nHistórico:\n").append(pastMessage);
        }

        contentBuilder.append("\nCliente: ").append(request.userInput());

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
            """.formatted(contentBuilder.toString());
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
