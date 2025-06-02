package com.fpt.metroll.shared.util;

import com.fpt.metroll.shared.domain.enums.AccountRole;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Supplier;

@Slf4j
public class SecurityUtil {

    public static boolean isAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }

    public static boolean isGuest() {
        return !isAuthenticated();
    }

    public static String requireUserId() {
        String userId = getUserId();
        if (userId == null) {
            throw new InsufficientAuthenticationException("User is not authenticated");
        }
        return userId;
    }

    @Nullable
    public static String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof String userId) {
                return userId;
            }
        }

        return null;
    }

    public static String requireUserEmail() {
        String email = getUserEmail();
        if (email == null) {
            throw new InsufficientAuthenticationException("User is not authenticated");
        }
        return email;
    }

    @Nullable
    public static String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getCredentials() instanceof String email) {
                return email;
            }
        }

        return null;
    }

    public static boolean hasRole(AccountRole... roles) {
        AccountRole currentUserRole = getUserRole();
        if (currentUserRole == null) {
            return false;
        }
        for (AccountRole role : roles) {
            if (role == currentUserRole) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public static AccountRole requireUserRole() {
        AccountRole role = getUserRole();
        if (role == null) {
            throw new InsufficientAuthenticationException("User is not authenticated");
        }
        return role;
    }

    private static final ThreadLocal<AccountRole> localRole = ThreadLocal.withInitial(() -> null);

    @Nullable
    public static AccountRole getUserRole() {
        if (localRole.get() != null) {
            return localRole.get();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof UsernamePasswordAuthenticationToken auth) {
            String role = auth.getAuthorities().iterator().next().getAuthority();
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            return AccountRole.valueOf(role);
        }

        return null;
    }

    public static void elevate(AccountRole role, Runnable action) {
        localRole.set(role);
        try {
            action.run();
        } finally {
            localRole.remove();
        }
    }

    public static <T> T elevate(AccountRole role, Supplier<T> action) {
        localRole.set(role);
        try {
            return action.get();
        } finally {
            localRole.remove();
        }
    }
}