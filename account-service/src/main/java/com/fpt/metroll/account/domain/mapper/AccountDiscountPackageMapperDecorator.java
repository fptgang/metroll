package com.fpt.metroll.account.domain.mapper;

import com.fpt.metroll.account.document.AccountDiscountPackage;
import com.fpt.metroll.account.service.AccountService;
import com.fpt.metroll.account.service.BlobStorageService;
import com.fpt.metroll.shared.domain.dto.discount.AccountDiscountPackageDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.google.cloud.storage.BlobId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public abstract class AccountDiscountPackageMapperDecorator implements AccountDiscountPackageMapper {
    
    @Autowired
    private AccountDiscountPackageMapper delegate;
    
    @Autowired
    private AccountService accountService;
    
    @Autowired
    private BlobStorageService blobStorageService;

    @Override
    public AccountDiscountPackageDto toDto(AccountDiscountPackage accountDiscountPackage) {
        AccountDiscountPackageDto dto = delegate.toDto(accountDiscountPackage);
        
        if (accountDiscountPackage != null) {
            // Populate the account field using AccountService.requireBasicById
            if (accountDiscountPackage.getAccountId() != null) {
                try {
                    SecurityUtil.elevate(AccountRole.ADMIN, () -> {
                        dto.setAccount(accountService.requireBasicById(accountDiscountPackage.getAccountId()));
                    });
                } catch (Exception e) {
                    // Log error but don't fail the mapping
                    log.warn("Failed to load account details for account ID: {}", accountDiscountPackage.getAccountId(), e);
                    dto.setAccount(null);
                }
            }
            
            // Generate signed URL for document if exists
            if (accountDiscountPackage.getProofDocumentUri() != null) {
                try {
                    String signedUrl = blobStorageService.generateSignedUrl(
                            BlobId.fromGsUtilUri(accountDiscountPackage.getProofDocumentUri())
                    ).toString();
                    dto.setDocumentUrl(signedUrl);
                } catch (Exception e) {
                    // Log error but don't fail the mapping
                    log.warn("Failed to generate signed URL for document: {}", accountDiscountPackage.getProofDocumentUri(), e);
                    dto.setDocumentUrl(null);
                }
            }
        }
        
        return dto;
    }
} 