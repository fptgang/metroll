package com.fpt.metroll.account.domain.mapper;

import com.fpt.metroll.account.document.DiscountPackage;
import com.fpt.metroll.account.domain.dto.DiscountPackageCreateRequest;
import com.fpt.metroll.account.domain.dto.DiscountPackageUpdateRequest;
import com.fpt.metroll.shared.domain.dto.discount.DiscountPackageDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DiscountPackageMapper {
    DiscountPackageDto toDto(DiscountPackage discountPackage);
    DiscountPackage toDocument(DiscountPackageDto dto);
    DiscountPackage toDocument(DiscountPackageCreateRequest dto);
    DiscountPackage toDocument(DiscountPackageUpdateRequest dto);
} 