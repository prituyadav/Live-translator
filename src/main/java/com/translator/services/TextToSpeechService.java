package com.translator.services;

import com.google.cloud.texttospeech.v1.*;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.OutputStream;

@Service
public class TextToSpeechService {

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

            // Save the audio to a file
            String outputFilePath = "output_" + System.currentTimeMillis() + ".mp3";
            try (OutputStream out = new FileOutputStream(outputFilePath)) {
                out.write(response.getAudioContent().toByteArray());
            }

            return outputFilePath;
        }
    }
}
