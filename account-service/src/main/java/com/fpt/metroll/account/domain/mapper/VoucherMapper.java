package com.fpt.metroll.account.domain.mapper;

import com.fpt.metroll.account.document.Voucher;
import com.fpt.metroll.account.domain.dto.VoucherCreateRequest;
import com.fpt.metroll.account.domain.dto.VoucherUpdateRequest;
import com.fpt.metroll.shared.domain.dto.voucher.VoucherDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.util.SecurityUtil;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VoucherMapper {
    VoucherDto toDto(Voucher voucher);
    Voucher toDocument(VoucherDto dto);
    Voucher toDocument(VoucherCreateRequest dto);
    Voucher toDocument(VoucherUpdateRequest dto);

    @AfterMapping
    default void redactCode(@MappingTarget VoucherDto dto) {
        if (!SecurityUtil.hasRole(AccountRole.CUSTOMER, AccountRole.ADMIN)) {
            dto.setCode(null);
        }
    }
} 