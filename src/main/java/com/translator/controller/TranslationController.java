package com.translator.controller;

import com.translator.models.TranslationRequest;
import com.translator.services.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TranslationController {

    @Autowired
    public TranslationService translationService;

    // Constructor

    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("welcome to the translator");
    }

    @PostMapping("/translate/text")
    public ResponseEntity<String> translateAndStoreAudio(@RequestBody TranslationRequest request) {
        try {
            String translated = translationService.translateAndStoreAudio(request);
            return ResponseEntity.ok("Translated Text: " + translated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
