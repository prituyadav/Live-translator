package com.translator.services;

import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    public List<String> getAllRecentUploadedFiles(String bucketName, int maxFiles) {
        List<String> fileNames = new ArrayList<>();

        try {
            Bucket bucket = storage.get(bucketName);
            if (bucket == null) {
                throw new RuntimeException("Bucket not found: " + bucketName);
            }

            // List all objects in the bucket
            for (Blob blob : bucket.list().iterateAll()) {
                fileNames.add(blob.getName());
                if (fileNames.size() >= maxFiles) {
                    break; // Limit the number of files returned
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching files from bucket: " + bucketName, e);
        }

        return fileNames;
    }

}
