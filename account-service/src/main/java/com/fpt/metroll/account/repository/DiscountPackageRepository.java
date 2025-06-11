package com.fpt.metroll.account.repository;

import com.fpt.metroll.account.document.DiscountPackage;
import com.fpt.metroll.shared.domain.enums.DiscountPackageStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscountPackageRepository extends MongoRepository<DiscountPackage, String> {
    List<DiscountPackage> findByStatus(DiscountPackageStatus status);
} 