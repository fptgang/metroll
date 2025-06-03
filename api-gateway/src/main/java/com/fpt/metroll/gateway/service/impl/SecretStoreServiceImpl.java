package com.fpt.metroll.gateway.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.metroll.gateway.service.SecretStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class SecretStoreServiceImpl implements SecretStoreService {
    private static final String AUTH_URL = "https://auth.idp.hashicorp.com/oauth2/token";
    private static final String AUDIENCE = "https://api.hashicorp.cloud";

    @Value("${HCP_CLIENT_ID}")
    private String clientId;

    @Value("${HCP_CLIENT_SECRET}")
    private String clientSecret;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public SecretStoreServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    public String getStatic(String secretUrl) throws IOException {
        String accessToken = getAccessToken();
        return getSecret(secretUrl, accessToken);
    }

    private String getAccessToken() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", "client_credentials");
        formData.add("audience", AUDIENCE);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        String response = restTemplate.postForObject(AUTH_URL, request, String.class);

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("access_token").asText();
    }

    private String getSecret(String secretUrl, String accessToken) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                secretUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        return jsonNode.get("secret").get("static_version").get("value").asText();
    }
} 