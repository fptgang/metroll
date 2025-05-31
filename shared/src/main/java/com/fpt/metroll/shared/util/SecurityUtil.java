package com.fpt.metroll.shared.util;

import com.fpt.metroll.shared.domain.enums.AccountRole;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
            } else {
                throw new InsufficientAuthenticationException("Unable to obtain AppUser");
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

    @Nullable
    public static AccountRole getUserRole() {
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
}