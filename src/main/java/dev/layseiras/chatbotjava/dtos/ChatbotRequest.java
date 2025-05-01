package dev.layseiras.chatbotjava.dtos;

import java.util.ArrayList;
import java.util.List;

public record ChatbotRequest(String userInput, List<String> context) {
    public ChatbotRequest {
        if (context == null) {
            context = new ArrayList<>();
        }
    }
}
