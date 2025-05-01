package dev.layseiras.chatbotjava.dtos;

import java.util.List;

public record ChatbotRequest(String userInput, List<String> context) {
}
