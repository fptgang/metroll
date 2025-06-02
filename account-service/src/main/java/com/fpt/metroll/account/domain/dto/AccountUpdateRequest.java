package com.fpt.metroll.account.domain.dto;

import com.fpt.metroll.shared.domain.enums.AccountRole;
import lombok.Data;

@Data
public class AccountUpdateRequest {
    private String fullName;
    private String phoneNumber;
    private AccountRole role;
}
