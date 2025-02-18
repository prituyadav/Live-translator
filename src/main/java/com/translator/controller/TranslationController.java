package com.translator.controller;

import com.translator.models.TextToSpeechRequest;
import com.translator.models.TranslationRequest;
import com.translator.services.StorageService;
import com.translator.services.TextToSpeechService;
import com.translator.services.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
    public ResponseEntity<String> translateAndStoreAudio(@RequestBody TranslationRequest request) {
        try {
            String translated = translationService.translateAndStoreAudio(request);
            return ResponseEntity.ok("Translated Text: " + translated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/audios/recent")
    public List<String> getAllRecentUploadedFiles(
            @RequestParam String bucketName,
            @RequestParam(defaultValue = "10") int maxFiles) {
        return storageService.getAllRecentUploadedFiles(bucketName, maxFiles);
    }

    @PostMapping("/texttospeech")
    public ResponseEntity<String> textToSpeech(@RequestBody TextToSpeechRequest request) {
        try {
            String audioFilePath =
                    textToSpeechService.convertTextToSpeech(request.getText(), request.getLanguageCode());
            return ResponseEntity.ok("Audio file created: " + audioFilePath);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(fileName).toAbsolutePath();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(404).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


}
