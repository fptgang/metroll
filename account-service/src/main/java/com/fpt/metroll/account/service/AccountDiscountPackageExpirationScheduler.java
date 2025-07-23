package com.fpt.metroll.account.service;

import com.fpt.metroll.account.document.AccountDiscountPackage;
import com.fpt.metroll.account.repository.AccountDiscountPackageRepository;
import com.fpt.metroll.shared.domain.enums.AccountDiscountStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class AccountDiscountPackageExpirationScheduler {

    private final AccountDiscountPackageRepository accountDiscountPackageRepository;

    public AccountDiscountPackageExpirationScheduler(AccountDiscountPackageRepository accountDiscountPackageRepository) {
        this.accountDiscountPackageRepository = accountDiscountPackageRepository;
    }

    @Scheduled(cron = "0 */15 * * * *") // Run every 15 minutes
    @Transactional
    public void processExpiredAccountDiscountPackages() {
        log.info("Starting account discount package expiration check...");
        
        try {
            Instant now = Instant.now();
            
            // Find all ACTIVATED account discount packages that have passed their validUntil date
            List<AccountDiscountPackage> expiredPackages = accountDiscountPackageRepository
                .findByStatusAndValidUntilBefore(AccountDiscountStatus.ACTIVATED, now);
            
            if (expiredPackages.isEmpty()) {
                log.debug("No expired account discount packages found.");
                return;
            }
            
            log.debug("Found {} expired account discount packages to process", expiredPackages.size());
            
            // Update status from ACTIVATED to EXPIRED
            expiredPackages.forEach(packageDoc -> packageDoc.setStatus(AccountDiscountStatus.EXPIRED));
            
            try {
                accountDiscountPackageRepository.saveAll(expiredPackages);
                log.info("Successfully processed {} expired account discount packages", expiredPackages.size());
            } catch (Exception e) {
                log.error("Failed to bulk update expired account discount packages: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("Error during account discount package expiration check: {}", e.getMessage(), e);
        }
    }
} 