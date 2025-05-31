package com.fpt.metroll.order.controller;

import com.fpt.metroll.shared.domain.client.AccountClient;
import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import com.fpt.metroll.shared.util.SecurityUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/")
@Tag(name = "Order", description = "Order API")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final AccountClient accountClient;

    public OrderController(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    @GetMapping
    public ResponseEntity<AccountDto> test() {
       return ResponseEntity.ok(accountClient.getAccount("1"));
    }
}
