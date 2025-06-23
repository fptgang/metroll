package com.fpt.metroll.account.repository;

import com.fpt.metroll.account.document.AccountDiscountPackage;
import com.fpt.metroll.shared.domain.enums.AccountDiscountStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountDiscountPackageRepository extends MongoRepository<AccountDiscountPackage, String> {
    List<AccountDiscountPackage> findByAccountId(String accountId);
    
    List<AccountDiscountPackage> findByAccountIdAndStatus(String accountId, AccountDiscountStatus status);
    
    Optional<AccountDiscountPackage> findByAccountIdAndStatusAndValidUntilAfter(
            String accountId, AccountDiscountStatus status, Instant currentTime);
    
    List<AccountDiscountPackage> findByDiscountPackageId(String discountPackageId);
} 