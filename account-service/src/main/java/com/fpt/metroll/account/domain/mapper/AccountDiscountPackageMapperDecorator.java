package com.fpt.metroll.account.domain.mapper;

import com.fpt.metroll.account.document.AccountDiscountPackage;
import com.fpt.metroll.account.service.AccountService;
import com.fpt.metroll.shared.domain.dto.discount.AccountDiscountPackageDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AccountDiscountPackageMapperDecorator implements AccountDiscountPackageMapper {
    
    @Autowired
    private AccountDiscountPackageMapper delegate;
    
    @Autowired
    private AccountService accountService;

    @Override
    public AccountDiscountPackageDto toDto(AccountDiscountPackage accountDiscountPackage) {
        AccountDiscountPackageDto dto = delegate.toDto(accountDiscountPackage);
        
        // Populate the account field using AccountService.requireBasicById
        if (accountDiscountPackage != null && accountDiscountPackage.getAccountId() != null) {
            try {
                SecurityUtil.elevate(AccountRole.ADMIN, () -> {
                    dto.setAccount(accountService.requireBasicById(accountDiscountPackage.getAccountId()));
                });
            } catch (Exception e) {
                // Log error but don't fail the mapping
                dto.setAccount(null);
            }
        }
        
        return dto;
    }
} 