package com.fpt.metroll.account.config;

import com.fpt.metroll.shared.service.SecretStoreService;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(GcpStorageConfig.GcpStorageProperties.class)
public class GcpStorageConfig {
    @Value("${HCP_SECRET_URL_GCP}")
    private String secretUrl;

    private final SecretStoreService secretStoreService;

    public GcpStorageConfig(SecretStoreService secretStoreService) {
        this.secretStoreService = secretStoreService;
    }

    @Bean
    public Storage storage(GcpStorageProperties properties) throws IOException {
        StorageOptions.Builder storageOptionsBuilder = StorageOptions.newBuilder()
                .setProjectId(properties.getProjectId());

        storageOptionsBuilder.setCredentials(
                com.google.auth.oauth2.ServiceAccountCredentials.fromStream(
                        new ByteArrayInputStream(secretStoreService.getStatic(secretUrl).getBytes(StandardCharsets.UTF_8))
                )
        );

        return storageOptionsBuilder.build().getService();
    }

    @ConfigurationProperties(prefix = "gcp.storage")
    public static class GcpStorageProperties {
        private String bucketName;
        private String credentialsPath;
        private String projectId;
        private Duration signedUrlDuration = Duration.ofMinutes(3);

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getCredentialsPath() {
            return credentialsPath;
        }

        public void setCredentialsPath(String credentialsPath) {
            this.credentialsPath = credentialsPath;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public Duration getSignedUrlDuration() {
            return signedUrlDuration;
        }

        public void setSignedUrlDuration(Duration signedUrlDuration) {
            this.signedUrlDuration = signedUrlDuration;
        }
    }
} 