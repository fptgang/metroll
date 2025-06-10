package com.fpt.metroll.account.document;

import com.fpt.metroll.shared.domain.enums.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Document(collection = "vouchers")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Voucher {
    @Id
    private String id;

    private String ownerId;

    @Indexed(unique = true)
    private String code;

    @Field(targetType=DECIMAL128)
    private BigDecimal discountAmount;

    @Field(targetType=DECIMAL128)
    private BigDecimal minTransactionAmount;

    private Instant validFrom;

    private Instant validUntil;

    private VoucherStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
} 