package com.fpt.metroll.account.controller;

import com.fpt.metroll.account.domain.dto.AccountDiscountAssignRequest;
import com.fpt.metroll.account.service.AccountDiscountPackageService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.discount.AccountDiscountPackageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/account-discount-packages")
@Tag(name = "Account Discount Package", description = "Account Discount Package API")
@SecurityRequirement(name = "bearerAuth")
public class AccountDiscountPackageController {

    private final AccountDiscountPackageService accountDiscountPackageService;

    public AccountDiscountPackageController(AccountDiscountPackageService accountDiscountPackageService) {
        this.accountDiscountPackageService = accountDiscountPackageService;
    }

    @Operation(summary = "List account discount packages by filter criteria")
    @GetMapping
    public ResponseEntity<PageDto<AccountDiscountPackageDto>> listAccountDiscountPackages(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "accountId", required = false) String accountId,
            @Parameter @RequestParam(name = "packageId", required = false) String packageId) {
        return ResponseEntity.ok(accountDiscountPackageService.findAll(accountId, packageId, pageableDto));
    }

    @Operation(summary = "Assign discount package to account")
    @PostMapping("/assign")
    public ResponseEntity<AccountDiscountPackageDto> assignDiscountPackage(
            @RequestBody @Valid AccountDiscountAssignRequest request) {
        return ResponseEntity.ok(accountDiscountPackageService.assign(request));
    }

    @Operation(summary = "Get account discount package by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AccountDiscountPackageDto> getAccountDiscountPackageById(@PathVariable("id") String id) {
        return ResponseEntity.ok(accountDiscountPackageService.requireById(id));
    }

    @Operation(summary = "Unassign discount package from account")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unassignDiscountPackage(@PathVariable("id") String id) {
        accountDiscountPackageService.unassign(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get my activated discount packages")
    @GetMapping("/my-discount")
    public ResponseEntity<AccountDiscountPackageDto> getMyActivatedDiscounts() {
        return ResponseEntity.ok(accountDiscountPackageService.findMyActivatedDiscounts());
    }

    @Operation(summary = "Get my discount percentage")
    @GetMapping("/my-discount-percentage")
    public ResponseEntity<Float> getMyDiscountPercentage() {
        return ResponseEntity.ok(accountDiscountPackageService.findMyDiscountPercentage());
    }
}