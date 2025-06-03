package com.fpt.metroll.gateway.util;

import com.google.common.base.Preconditions;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public final class FirebaseFakeProfileRegistryImpl implements FirebaseFakeProfileRegistry {

    @Value("${metroll.auth.fake-firebase-profiles:false}")
    private boolean enableFakeFirebaseProfiles;

    private FirebaseToken mockFirebaseToken(String accountId, String role) {
        try {
            var claims = Map.of(
                    "sub", accountId,
                    "name", "Mock " + accountId,
                    "picture", "https://example.com/mock.jpg",
                    "email", "mock-"+accountId+"@example.com",
                    "email_verified", true,
                    "iss", "https://securetoken.google.com/project-id",
                    "role", role
            );

            var cons = FirebaseToken.class.getDeclaredConstructor(Map.class);
            cons.setAccessible(true);
            return cons.newInstance(claims);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load firebase fake profiles", e);
        }
    }

    @Override
    public FirebaseToken verifyIdToken(String token) {
        // e.g. $mock:681c4a0a45880c5c0a58e6ed[:CUSTOMER]
        if (!enableFakeFirebaseProfiles || !token.startsWith("$mock:")) {
            return null;
        }

        String[] params = token.substring("$mock:".length()).split(":");
        String accountId = params[0];
        Preconditions.checkArgument(!accountId.isBlank(), "Invalid account ID");
        String role = params.length < 2 ? "CUSTOMER" : params[1];
        return mockFirebaseToken(accountId, role);
    }
}

