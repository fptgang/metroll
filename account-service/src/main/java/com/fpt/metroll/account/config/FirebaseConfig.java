package com.fpt.metroll.account.config;

import com.fpt.metroll.shared.service.SecretStoreService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;

@Configuration
public class FirebaseConfig {
    private final SecretStoreService secretStoreService;

    @Value("${HCP_SECRET_URL_FIREBASE}")
    private String secretUrl;

    public FirebaseConfig(SecretStoreService secretStoreService) {
        this.secretStoreService = secretStoreService;
    }

    @PostConstruct
    public void initialize() {
        try {
            String credentialJson = secretStoreService.getStatic(secretUrl);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(credentialJson.getBytes())))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}
