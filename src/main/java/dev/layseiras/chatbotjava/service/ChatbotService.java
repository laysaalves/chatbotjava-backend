package dev.layseiras.chatbotjava.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.layseiras.chatbotjava.config.GeminiApi;
import dev.layseiras.chatbotjava.dtos.ChatbotRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final String apiUrl;
    private final String apiKey;

    private final WebClient webClient;

    private final ReadFileService fileService;

    @Autowired
    public ChatbotService(GeminiApi gemini, ReadFileService fileService) {
        this.apiKey = gemini.getApiKey();
        this.apiUrl = gemini.getApiUrl() + apiKey;
        this.fileService = fileService;

        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("content-type", "application/json")
                .build();
    }

    public String processUserInput(ChatbotRequest request) {
        String systemPrompt = buildSystemPrompt();
        String contextHistory = buildContextHistory(request.context());
        String userPrompt = "Cliente: " + request.userInput();

        String fullPrompt = String.join("\n", systemPrompt, contextHistory, userPrompt);

        return wrapInJson(fullPrompt);
    }

        private static final String SYSTEM_INSTRUCTIONS = """
        Você é um agente virtual da loja Layseiras Shop. 
        Sua função é atender os clientes com simpatia, clareza e objetividade, utilizando as informações da loja a seguir. 
        Responda perguntas sobre produtos, prazos, políticas de frete e formas de pagamento com base nesses dados. 
        Se não souber algo, oriente o cliente a entrar em contato pelo e-mail oficial ou WhatsApp da loja.
        
        %s
        """;

        private String buildSystemPrompt() {
            String storeData = fileService.getTextFile();
            return SYSTEM_INSTRUCTIONS.formatted(storeData);
        }


    private String buildContextHistory(List<String> contextMessages) {
        if (contextMessages == null || contextMessages.isEmpty()) return "";

        return contextMessages.stream()
                .map(msg -> "Histórico:\n" + msg)
                .collect(Collectors.joining("\n"));
    }

    private String wrapInJson(String prompt) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            ObjectNode part = mapper.createObjectNode();
            part.put("text", prompt);

            ArrayNode parts = mapper.createArrayNode().add(part);
            ObjectNode content = mapper.createObjectNode();
            content.set("parts", parts);

            ArrayNode contents = mapper.createArrayNode().add(content);
            ObjectNode root = mapper.createObjectNode();
            root.set("contents", contents);

            return mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao montar JSON do input", e);
        }
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

                        if (candidates.isArray() && !candidates.isEmpty()) {
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
