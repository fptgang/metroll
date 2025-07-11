package com.fpt.metroll.account.controller;

import com.fpt.metroll.account.domain.dto.AccountCreateRequest;
import com.fpt.metroll.account.domain.dto.AccountDashboardDto;
import com.fpt.metroll.account.domain.dto.AccountUpdateRequest;
import com.fpt.metroll.account.domain.dto.StationAssignRequest;
import com.fpt.metroll.account.service.AccountService;
import com.fpt.metroll.account.service.AuthService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import com.fpt.metroll.shared.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/accounts")
@Tag(name = "Account", description = "Account API")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;
    private final AuthService authService;

    public AccountController(AccountService accountService, AuthService authService) {
        this.accountService = accountService;
        this.authService = authService;
    }

    @Operation(summary = "Get account service dashboard statistics")
    @GetMapping("/dashboard")
    public ResponseEntity<AccountDashboardDto> getDashboard() {
        return ResponseEntity.ok(accountService.getDashboard());
    }

    @Operation(summary = "Login")
    @PostMapping("/login/")
    public ResponseEntity<AccountDto> login() {
        return ResponseEntity.ok(authService.login());
    }

    @Operation(summary = "It's me...")
    @GetMapping("/me/")
    public ResponseEntity<AccountDto> me() {
        return ResponseEntity.ok(accountService.requireById(SecurityUtil.requireUserId()));
    }

    @Operation(summary = "List accounts by search & filter criteria")
    @GetMapping
    public ResponseEntity<PageDto<AccountDto>> listAccounts(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(accountService.findAll(search, pageableDto));
    }

    @Operation(summary = "List staff accounts by search criteria (Admin only)")
    @GetMapping("/staff")
    public ResponseEntity<PageDto<AccountDto>> listStaff(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(accountService.findStaff(search, pageableDto));
    }

    @Operation(summary = "Create account")
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody @Valid AccountCreateRequest request) {
        return ResponseEntity.ok(accountService.create(request));
    }

    @Operation(summary = "Get account by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable("id") String id) {
        return ResponseEntity.ok(accountService.requireById(id));
    }

    @Operation(summary = "Update account")
    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable("id") String id,
            @RequestBody @Valid AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.update(id, request));
    }

    @Operation(summary = "Assign station to staff")
    @PutMapping("/{id}/assign-station")
    public ResponseEntity<AccountDto> assignStation(@PathVariable("id") String id,
            @RequestBody @Valid StationAssignRequest request) {
        return ResponseEntity.ok(accountService.assignStation(id, request));
    }

    @Operation(summary = "Deactivate account")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateAccount(@PathVariable("id") String id) {
        accountService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activate account")
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateAccount(@PathVariable("id") String id) {
        accountService.activate(id);
        return ResponseEntity.noContent().build();
    }
}
