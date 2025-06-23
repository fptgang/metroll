package com.fpt.metroll.account.document;

import com.fpt.metroll.shared.domain.enums.DiscountPackageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.data.mongodb.core.mapping.FieldType.DECIMAL128;

@Document(collection = "discountPackages")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscountPackage {
    @Id
    private String id;

    private String name;

    private String description;

    private Float discountPercentage;

    private Integer duration;

    private DiscountPackageStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
} 