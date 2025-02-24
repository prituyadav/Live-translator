package com.translator.services;

import com.google.cloud.speech.v1.*;
import com.google.cloud.storage.*;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.protobuf.ByteString;
import com.translator.models.TranslationRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {

    private static final String bucketName = "live-translator-audio-bucket";

    private final Translate translate;
    private final TextToSpeechService ttsService;
    private final StorageService storageService;

    public TranslationService(TextToSpeechService ttsService, StorageService storageService) {
        this.translate = TranslateOptions.getDefaultInstance().getService();
        this.ttsService = ttsService;
        this.storageService = storageService;
    }

    public Map<String, String> translateAndStoreAudio(TranslationRequest request) throws Exception {
        Map<String, String> map = new HashMap<>();

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
        String fileName = storageService.uploadAudioToBucket(
                bucketName,
                audioContent,
                request.getAudioFormat()
        );

        String translatedAudio = storageService.getPublicDownloadUrl(fileName);
        map.put(translatedText, translatedAudio);
        return map;
    }


    public Map<String, String> uploadAndTranscribe(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        String publicUrl = uploadToCloudStorage(fileName, file.getBytes());
        byte[] fileContent = downloadFileFromCloudStorage(fileName);
        String transcribedText = transcribeAudio(fileContent);

        Map<String, String> result = new HashMap<>();
        result.put("publicUrl", publicUrl);
        result.put("transcribedText", transcribedText);
        return result;
    }

    private String uploadToCloudStorage(String fileName, byte[] fileContent) throws Exception {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("audio/mpeg")
                .build();
        storage.create(blobInfo, fileContent);
        return "https://storage.googleapis.com/" + bucketName + "/" + fileName;
    }

    private byte[] downloadFileFromCloudStorage(String fileName) throws Exception {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        BlobId blobId = BlobId.of(bucketName, fileName);
        Blob blob = storage.get(blobId);
        if (blob == null) {
            throw new Exception("File not found: " + fileName);
        }
        return blob.getContent();
    }

    private String transcribeAudio(byte[] audioContent) throws Exception {
        try (SpeechClient speechClient = SpeechClient.create()) {
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(audioContent))
                    .build();
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setLanguageCode("en-US")
                    .build();
            RecognizeResponse response = speechClient.recognize(config, audio);
            StringBuilder transcribedText = new StringBuilder();
            for (SpeechRecognitionResult result : response.getResultsList()) {
                transcribedText.append(result.getAlternatives(0).getTranscript());
            }
            return transcribedText.toString();
        }
    }

}
