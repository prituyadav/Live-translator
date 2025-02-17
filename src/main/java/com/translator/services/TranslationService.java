package com.translator.services;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.translator.models.TranslationRequest;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    private final Translate translate;
    private final TextToSpeechService ttsService;
    private final StorageService storageService;

    public TranslationService(TextToSpeechService ttsService, StorageService storageService) {
        this.translate = TranslateOptions.getDefaultInstance().getService();
        this.ttsService = ttsService;
        this.storageService = storageService;
    }

    public String translateAndStoreAudio(TranslationRequest request) throws Exception {
        // Translate text
        Translation translation = translate.translate(
                request.getText(),
                Translate.TranslateOption.sourceLanguage(request.getSourceLang()),
                Translate.TranslateOption.targetLanguage(request.getTargetLang())
        );

        String translatedText = translation.getTranslatedText();

        // Convert to audio
        byte[] audioContent = ttsService.convertTextToAudio(
                translatedText,
                request.getTargetLang(),
                request.getVoiceName(),
                request.getAudioFormat()
        );

        // Store in Cloud Storage
        storageService.uploadAudioToBucket(
                request.getBucketName(),
                audioContent,
                request.getAudioFormat()
        );
        return translatedText;

    }
}
