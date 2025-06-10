package com.fpt.metroll.account.service.impl;

import com.fpt.metroll.account.domain.dto.AccountCreateRequest;
import com.fpt.metroll.account.service.AccountService;
import com.fpt.metroll.account.service.AuthService;
import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private final AccountService accountService;
    private final StreamBridge streamBridge;

    public AuthServiceImpl(AccountService accountService, StreamBridge streamBridge) {
        this.accountService = accountService;
        this.streamBridge = streamBridge;
    }

    @Override
    @Transactional
    public AccountDto login() {
        String email = SecurityUtil.requireUserEmail();
        String uid = SecurityUtil.getUserId();

        AccountDto account = SecurityUtil.elevate(AccountRole.ADMIN, () -> {
            return accountService.findById(uid).orElseGet(() -> {
                return accountService.create(uid, AccountCreateRequest.builder()
                        .email(email)
                        .fullName(email)
                        .phoneNumber("")
                        .role(AccountRole.CUSTOMER)
                        .build());
            });
        });

        if (!email.contains("@example.com")) { // @example.com are mock accounts
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", account.getRole().toString());
            try {
                FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);
            } catch (FirebaseAuthException e) {
                throw new IllegalStateException("Failed to set custom user claims", e);
            }
        }

        // Publish login attempt
        Map<String, Object> loginAttempt = new HashMap<>();
        loginAttempt.put("userId", uid);
        loginAttempt.put("email", email);
        loginAttempt.put("timestamp", LocalDateTime.now().toString());
        loginAttempt.put("role", account.getRole().toString());
        streamBridge.send("loginAttempt-out-0", loginAttempt);

        return account;
    }
}
