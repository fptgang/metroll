package com.fpt.metroll.account.document;

import com.fpt.metroll.shared.domain.enums.AccountDiscountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "accountDiscountPackages")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDiscountPackage {
    @Id
    private String id;

    private String accountId;

    private String discountPackageId;

    private Instant activateDate;

    private Instant validUntil;

    private AccountDiscountStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
} 