package com.fpt.metroll.account.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDiscountAssignRequest {
    @NotBlank(message = "Account ID is required")
    private String accountId;

    @NotBlank(message = "Discount package ID is required")
    private String discountPackageId;
    
    @NotNull(message = "Document file is required")
    private MultipartFile document;
} 