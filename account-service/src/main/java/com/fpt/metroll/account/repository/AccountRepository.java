package com.fpt.metroll.account.repository;

import com.fpt.metroll.account.document.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    boolean existsByEmail(String email);
    Account findByEmail(String email);
}