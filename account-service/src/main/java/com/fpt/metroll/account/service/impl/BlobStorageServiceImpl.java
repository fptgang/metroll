package com.fpt.metroll.account.service.impl;

import com.fpt.metroll.account.config.GcpStorageConfig;
import com.fpt.metroll.account.service.BlobStorageService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class BlobStorageServiceImpl implements BlobStorageService {

    private final Storage storage;
    private final GcpStorageConfig.GcpStorageProperties properties;

    public BlobStorageServiceImpl(Storage storage, GcpStorageConfig.GcpStorageProperties properties) {
        this.storage = storage;
        this.properties = properties;
    }

    @Override
    public BlobId uploadFile(MultipartFile file, String folderPath) {
        Preconditions.checkNotNull(file, "File cannot be null");
        Preconditions.checkArgument(!file.isEmpty(), "File cannot be empty");
        Preconditions.checkNotNull(folderPath, "Folder path cannot be null");

        try {
            // Generate unique filename with timestamp and UUID
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String fileName = folderPath + "/" + 
                             Instant.now().getEpochSecond() + "_" + 
                             UUID.randomUUID().toString() + 
                             extension;

            BlobId blobId = BlobId.of(properties.getBucketName(), fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            // Upload the file
            storage.create(blobInfo, file.getBytes());
            
            log.info("File uploaded successfully: bucket={}, name={}", 
                    blobId.getBucket(), blobId.getName());
            
            return blobId;
            
        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public void deleteFile(BlobId blobId) {
        Preconditions.checkNotNull(blobId, "BlobId cannot be null");

        try {
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                log.info("File deleted successfully: bucket={}, name={}", 
                        blobId.getBucket(), blobId.getName());
            } else {
                log.warn("File not found for deletion: bucket={}, name={}", 
                        blobId.getBucket(), blobId.getName());
            }
        } catch (Exception e) {
            log.error("Failed to delete file: bucket={}, name={}, error={}", 
                    blobId.getBucket(), blobId.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Override
    public URL generateSignedUrl(BlobId blobId) {
        Preconditions.checkNotNull(blobId, "BlobId cannot be null");

        try {
            // Check if blob exists
            Blob blob = storage.get(blobId);
            if (blob == null || !blob.exists()) {
                throw new IllegalArgumentException("File does not exist: " + blobId.getName());
            }

            // Generate signed URL
            URL signedUrl = storage.signUrl(
                    BlobInfo.newBuilder(blobId).build(),
                    properties.getSignedUrlDuration().toMinutes(),
                    TimeUnit.MINUTES,
                    Storage.SignUrlOption.httpMethod(HttpMethod.GET)
            );

            log.debug("Generated signed URL for: bucket={}, name={}", 
                    blobId.getBucket(), blobId.getName());
            
            return signedUrl;
            
        } catch (Exception e) {
            log.error("Failed to generate signed URL: bucket={}, name={}, error={}", 
                    blobId.getBucket(), blobId.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate signed URL", e);
        }
    }
} 