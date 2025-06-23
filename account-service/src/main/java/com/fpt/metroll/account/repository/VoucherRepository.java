package com.fpt.metroll.account.repository;

import com.fpt.metroll.account.document.Voucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {
    boolean existsByCode(String code);
} 