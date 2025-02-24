package com.translator.services;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.texttospeech.v1.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class TextToSpeechService {

    private static final String BUCKET_NAME = "live-translator-audio-bucket";

    public byte[] convertTextToAudio(String text, String languageCode, String voiceName, String audioFormat)
            throws Exception {

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode)
                    .setName(voiceName)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.valueOf(audioFormat))
                    .build();

            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(
                    input, voice, audioConfig);

            return response.getAudioContent().toByteArray();
        }
    }

    public String convertTextToSpeech(String text, String languageCode) throws Exception {
        // Initialize the Text-to-Speech client
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Configure the voice request
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(languageCode) // e.g., "en-US"
                    .setSsmlGender(SsmlVoiceGender.NEUTRAL) // Voice gender
                    .build();

            // Configure the audio format
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3) // Output audio format
                    .build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Generate a unique file name
            String fileName = "output_" + System.currentTimeMillis() + ".mp3";

            // Save the audio to Google Cloud Storage
            uploadToCloudStorage(fileName, response.getAudioContent().toByteArray());

            return getPublicDownloadUrl(fileName);
        }
    }

    private void uploadToCloudStorage(String fileName, byte[] audioContent) throws Exception {
        // Initialize the Cloud Storage client
        Storage storage = StorageOptions.getDefaultInstance().getService();

        // Define the BlobId and BlobInfo
        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("audio/mpeg") // Set the MIME type for MP3 files
                .build();

        // Upload the audio content to Cloud Storage
        try (InputStream inputStream = new ByteArrayInputStream(audioContent)) {
            storage.create(blobInfo, inputStream);
        }
    }

    public String getPublicDownloadUrl(String fileName) {
        return String.format("https://storage.googleapis.com/%s/%s", BUCKET_NAME, fileName);
    }
}
