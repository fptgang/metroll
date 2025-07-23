package com.fpt.metroll.account.service;

import com.fpt.metroll.account.document.Voucher;
import com.fpt.metroll.account.repository.VoucherRepository;
import com.fpt.metroll.shared.domain.enums.VoucherStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class VoucherExpirationScheduler {

    private final VoucherRepository voucherRepository;

    public VoucherExpirationScheduler(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour at minute 0
    @Transactional
    public void processExpiredVouchers() {
        log.info("Starting voucher expiration check...");
        
        try {
            Instant now = Instant.now();
            
            // Find all VALID vouchers that have passed their validUntil date
            List<Voucher> expiredVouchers = voucherRepository.findByStatusAndValidUntilBefore(
                VoucherStatus.VALID, now);
            
            if (expiredVouchers.isEmpty()) {
                log.debug("No expired vouchers found.");
                return;
            }
            
            log.debug("Found {} expired vouchers to process", expiredVouchers.size());
            
            expiredVouchers.forEach(voucher -> voucher.setStatus(VoucherStatus.EXPIRED));
            
            try {
                voucherRepository.saveAll(expiredVouchers);
                log.info("Successfully processed {} expired vouchers", expiredVouchers.size());
            } catch (Exception e) {
                log.error("Failed to bulk update expired vouchers: {}", e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("Error during voucher expiration check: {}", e.getMessage(), e);
        }
    }
} 