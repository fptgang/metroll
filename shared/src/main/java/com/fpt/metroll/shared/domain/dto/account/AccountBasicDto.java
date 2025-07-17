package com.fpt.metroll.shared.domain.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBasicDto {
    private String id;
    private String email;
    private String fullName;
}
