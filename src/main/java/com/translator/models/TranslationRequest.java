package com.translator.models;

import lombok.Data;

@Data
public class TranslationRequest {
    private String text;
    private String sourceLang;
    private String targetLang;
    private String voiceName = "en-US-Wavenet-D"; // Default voice
    private String audioFormat = "MP3"; // Default format
}
