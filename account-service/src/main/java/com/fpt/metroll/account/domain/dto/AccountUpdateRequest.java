package com.fpt.metroll.account.domain.dto;

import com.fpt.metroll.shared.domain.enums.AccountRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountUpdateRequest {
    private String fullName;
    private String phoneNumber;
    private AccountRole role;
}
