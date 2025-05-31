package com.fpt.metroll.account.controller;

import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import com.fpt.metroll.shared.util.SecurityUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/")
@Tag(name = "Account", description = "Account API")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {


    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable("accountId") String accountId) {
        return ResponseEntity.ok(AccountDto.builder()
                .id(accountId)
                .build());
    }

    @GetMapping
    public String test() {
        if (SecurityUtil.isGuest()) {
            return "Hello guest";
        }
        return "Hello user: " + SecurityUtil.getUserId() + " " + SecurityUtil.getUserRole();
    }
}
