package com.fpt.metroll.shared.domain.dto.account;

import com.fpt.metroll.shared.domain.enums.AccountRole;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class AccountDto {
    private String id;
    private String nickname;
    private String email;
    private AccountRole role;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
