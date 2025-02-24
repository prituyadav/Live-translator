package com.translator.controller;

import com.translator.models.TextToSpeechRequest;
import com.translator.models.TranslationRequest;
import com.translator.services.StorageService;
import com.translator.services.TextToSpeechService;
import com.translator.services.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TranslationController {

    @Autowired
    public TranslationService translationService;
    private StorageService storageService;
    private TextToSpeechService textToSpeechService;


    public TranslationController(StorageService storageService, TextToSpeechService textToSpeechService) {
        this.storageService = storageService;
        this.textToSpeechService = textToSpeechService;
    }

    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("welcome to the translator");
    }

    @PostMapping("/translate/text")
    public ResponseEntity<Map<String, String>> translateAndStoreAudio(@RequestBody TranslationRequest request) {
        try {
            Map<String, String> translated = translationService.translateAndStoreAudio(request);
            return ResponseEntity.ok(translated);
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/audios/recent")
    public List<String> getAllRecentUploadedFiles(
            @RequestParam(defaultValue = "10") int maxFiles) {
        return storageService.getAllRecentUploadedFiles(maxFiles);
    }

    @PostMapping("/texttospeech")
    public ResponseEntity<String> textToSpeech(@RequestBody TextToSpeechRequest request) {
        try {
            String audioFilePath =
                    textToSpeechService.convertTextToSpeech(request.getText(), request.getLanguageCode());
            return ResponseEntity.ok(audioFilePath);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/audios/enable/access")
    public String getAllRecentUploadedFiles() {
        storageService.enableUniformBucketLevelAccess();
        return "successfully enabled";
    }


    @PostMapping("/transcribe")
    public ResponseEntity<Map<String, String>> uploadAndTranscribe(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, String> result = translationService.uploadAndTranscribe(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

}


