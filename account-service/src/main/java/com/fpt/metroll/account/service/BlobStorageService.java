package com.fpt.metroll.account.service;

import com.google.cloud.storage.BlobId;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;

public interface BlobStorageService {
    
    /**
     * Upload a file to GCP Cloud Storage
     * @param file the file to upload
     * @param folderPath the folder path within the bucket
     * @return the BlobId of the uploaded file
     */
    BlobId uploadFile(MultipartFile file, String folderPath);
    
    /**
     * Delete a file from GCP Cloud Storage
     * @param blobId the BlobId of the file to delete
     */
    void deleteFile(BlobId blobId);
    
    /**
     * Generate a signed URL for temporary file access
     * @param blobId the BlobId of the file
     * @return the signed URL valid for the configured duration
     */
    URL generateSignedUrl(BlobId blobId);
} 