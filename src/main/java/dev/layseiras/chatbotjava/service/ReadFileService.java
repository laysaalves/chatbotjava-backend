package dev.layseiras.chatbotjava.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ReadFileService {
    @Value("classpath:loja-info.txt")
    private Resource file;

    private String textFile;

    @PostConstruct
    public void init() {
        try {
            byte[] bytes = file.getInputStream().readAllBytes();
            textFile = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            textFile = "Informações indisponíveis no momento.";
        }
    }

    public String getTextFile() {
        return textFile;
    }
}
