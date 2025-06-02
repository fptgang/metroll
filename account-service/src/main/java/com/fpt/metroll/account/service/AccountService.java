package com.fpt.metroll.account.service;

import com.fpt.metroll.account.domain.dto.AccountCreateRequest;
import com.fpt.metroll.account.domain.dto.AccountUpdateRequest;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.account.AccountDto;

import java.util.Optional;

public interface AccountService {
    PageDto<AccountDto> findAll(String search, PageableDto pageable);
    Optional<AccountDto> findById(String id);
    AccountDto requireById(String id);
    AccountDto create(AccountCreateRequest request);
    AccountDto update(String id, AccountUpdateRequest request);
    void deactivate(String id);
}
