package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service", contextId = "accountClient")
public interface AccountClient {

    @GetMapping("/account/accounts/{accountId}")
    AccountDto getAccount(@PathVariable("accountId") String accountId);
}
