package com.translator.services;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class StorageService {

    private final Storage storage;

    public StorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public String uploadAudioToBucket(String bucketName, byte[] audioContent, String audioFormat) {
        String fileName = "translated_audio_" + UUID.randomUUID() + "." + audioFormat.toLowerCase();
        String contentType = "audio/" + audioFormat.toLowerCase();

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        storage.create(blobInfo, audioContent);
        return fileName;
    }
}
