package com.fpt.metroll.account.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDiscountAssignRequest {
    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotBlank(message = "Discount package ID is required")
    private String discountPackageId;
} 