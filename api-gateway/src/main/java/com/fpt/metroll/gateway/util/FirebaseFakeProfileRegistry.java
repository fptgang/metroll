package com.fpt.metroll.gateway.util;

import com.google.firebase.auth.FirebaseToken;

public interface FirebaseFakeProfileRegistry {
    FirebaseToken verifyIdToken(String token);
}

