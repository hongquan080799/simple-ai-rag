package com.example.chat_ai.controller;

import com.example.chat_ai.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@RestController
public class AiController {
    @Autowired
    private AIService aiService;
    @PostMapping("/ai")
    public Map<String, String> chatbot(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        String response = aiService.chat(query);
        return Map.of("response", response);
    }

    @PostMapping("/ai-rag")
    public Map<String, String> askPdf(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        String response = aiService.chatWithRag(query);
        return Map.of("response", response);
    }

    @PostMapping("/embedding")
    public Map<String, Object> pdfUpload(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String PDF_SAVE_PATH = "documents/";

            Path saveDirectory = Paths.get(PDF_SAVE_PATH);
            if (!Files.exists(saveDirectory)) {
                Files.createDirectories(saveDirectory);
            }
            Path filePath = saveDirectory.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            aiService.embeddDocument();

            // Return response
            return Map.of(
                    "status", "Successfully Uploaded",
                    "filename", fileName
            );

        } catch (Exception e) {
            return Map.of("status", "Error", "message", e.getMessage());
        }
    }
}
