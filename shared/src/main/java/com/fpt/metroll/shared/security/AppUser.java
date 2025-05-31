package com.fpt.metroll.shared.security;

import com.fpt.metroll.shared.domain.enums.AccountRole;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AppUser extends User {
    @Getter
    private final String accountId;
    private final AccountRole role;

    public AppUser(String accountId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);

        if (authorities.isEmpty())
            throw new IllegalArgumentException("Authorities cannot be empty");

        this.accountId = accountId;
        String authority = getAuthorities().iterator().next().getAuthority();
        role = AccountRole.valueOf(authority.startsWith("ROLE_") ? authority.substring("ROLE_".length()) : authority);
    }

    @NotNull
    public AccountRole getRole() {
        return role;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // a logged in account should be visible already
    }
}

