package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service")
public interface AccountClient {

    @GetMapping("/account/{accountId}")
    AccountDto getAccount(@PathVariable("accountId") String accountId);
}
