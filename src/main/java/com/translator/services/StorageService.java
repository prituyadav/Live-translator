package com.translator.services;

import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StorageService {

    private static final String bucketName = "live-translator-audio-bucket";
    private static final String projectId = "live-translator-451219";

    private final Storage storage;

    public StorageService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public static void enableUniformBucketLevelAccess() {

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

        Bucket bucket = storage.get(bucketName);

        BucketInfo.IamConfiguration iamConfiguration =
                BucketInfo.IamConfiguration.newBuilder().setIsUniformBucketLevelAccessEnabled(true).build();

        storage.update(
                bucket
                        .toBuilder()
                        .setIamConfiguration(iamConfiguration)
                        .setAcl(null)
                        .setDefaultAcl(null)
                        .build(),
                Storage.BucketTargetOption.metagenerationMatch());

        System.out.println("Uniform bucket-level access was enabled for " + bucketName);
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

    public List<String> getAllRecentUploadedFiles(int maxFiles) {
        List<Blob> blobs = new ArrayList<>();

        try {
            Bucket bucket = storage.get(bucketName);
            if (bucket == null) {
                throw new RuntimeException("Bucket not found: " + bucketName);
            }

            // Collect all objects in the bucket
            for (Blob blob : bucket.list().iterateAll()) {
                blobs.add(blob);
            }

            // Sort blobs by creation time in descending order (most recent first)
            blobs.sort((b1, b2) -> b2.getCreateTime().compareTo(b1.getCreateTime())); // FIXED COMPARISON

            // Extract file names (or public URLs)
            return blobs.stream()
                    .limit(maxFiles)
                    .map(blob -> getPublicDownloadUrl(blob.getName()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error fetching files from bucket: " + bucketName, e);
        }
    }

    public String getPublicDownloadUrl(String fileName) {
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
    }


}
