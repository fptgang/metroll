package com.fpt.metroll.account.repository;

import com.fpt.metroll.account.document.Voucher;
import com.fpt.metroll.shared.domain.enums.VoucherStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {
    boolean existsByCode(String code);

    Optional<Voucher> findByCode(String code);
    
    List<Voucher> findByStatusAndValidUntilBefore(VoucherStatus status, Instant expiredDate);
}