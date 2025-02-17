package com.translator.services;

import com.google.cloud.texttospeech.v1.*;
import org.springframework.stereotype.Service;

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
}
