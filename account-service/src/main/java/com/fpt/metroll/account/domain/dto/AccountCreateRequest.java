package com.fpt.metroll.account.domain.dto;

import com.fpt.metroll.shared.domain.enums.AccountRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountCreateRequest {
    private String id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private AccountRole role;
}
