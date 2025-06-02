package com.fpt.metroll.account.domain.mapper;

import com.fpt.metroll.account.document.Account;
import com.fpt.metroll.account.domain.dto.AccountCreateRequest;
import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountDto toDto(Account account);
    Account toDocument(AccountDto dto);
    Account toDocument(AccountCreateRequest dto);
}
