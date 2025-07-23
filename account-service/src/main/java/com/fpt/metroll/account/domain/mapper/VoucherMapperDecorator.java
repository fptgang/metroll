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

        if (voucher != null && voucher.getUserId() != null) {
            try {
                SecurityUtil.elevate(AccountRole.ADMIN, () -> {
                    dto.setUser(accountService.requireBasicById(voucher.getUserId()));
                });
            } catch (Exception e) {
                // Log error but don't fail the mapping
                dto.setUser(null);
            }
        }

        if (voucher != null && voucher.getIssuerId() != null) {
            try {
                SecurityUtil.elevate(AccountRole.ADMIN, () -> {
                    dto.setIssuer(accountService.requireBasicById(voucher.getIssuerId()));
                });
            } catch (Exception e) {
                // Log error but don't fail the mapping
                dto.setIssuer(null);
            }
        }
        
        return dto;
    }
} 