package com.fpt.metroll.account.domain.mapper;

import com.fpt.metroll.account.document.AccountDiscountPackage;
import com.fpt.metroll.shared.domain.dto.discount.AccountDiscountPackageDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@DecoratedWith(AccountDiscountPackageMapperDecorator.class)
public interface AccountDiscountPackageMapper {
    AccountDiscountPackageDto toDto(AccountDiscountPackage accountDiscountPackage);
    AccountDiscountPackage toDocument(AccountDiscountPackageDto dto);
} 