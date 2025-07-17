package com.fpt.metroll.account.domain.mapper;

import com.fpt.metroll.account.document.Voucher;
import com.fpt.metroll.account.service.AccountService;
import com.fpt.metroll.shared.domain.dto.voucher.VoucherDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class VoucherMapperDecorator implements VoucherMapper {
    
    @Autowired
    private VoucherMapper delegate;
    
    @Autowired
    private AccountService accountService;

    @Override
    public VoucherDto toDto(Voucher voucher) {
        VoucherDto dto = delegate.toDto(voucher);
        
        // Populate the owner field using AccountService.requireBasicById
        if (voucher != null && voucher.getOwnerId() != null) {
            try {
                SecurityUtil.elevate(AccountRole.ADMIN, () -> {
                    dto.setOwner(accountService.requireBasicById(voucher.getOwnerId()));
                });
            } catch (Exception e) {
                // Log error but don't fail the mapping
                dto.setOwner(null);
            }
        }
        
        return dto;
    }
} 