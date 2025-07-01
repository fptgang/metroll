package com.fpt.metroll.shared.domain.dto.account;

import com.fpt.metroll.shared.domain.enums.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private String id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private AccountRole role;
    private Boolean active;
    private String assignedStation;
    private Instant createdAt;
    private Instant updatedAt;
}
