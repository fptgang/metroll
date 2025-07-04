package com.fpt.metroll.account.controller;

import com.fpt.metroll.account.domain.dto.VoucherCreateRequest;
import com.fpt.metroll.account.domain.dto.VoucherUpdateRequest;
import com.fpt.metroll.account.service.VoucherService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.voucher.VoucherDto;
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
@RequestMapping("/vouchers")
@Tag(name = "Voucher", description = "Voucher API")
@SecurityRequirement(name = "bearerAuth")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @Operation(summary = "List vouchers by search & filter criteria")
    @GetMapping
    public ResponseEntity<PageDto<VoucherDto>> listVouchers(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "userId", required = false) String userId) {
        return ResponseEntity.ok(voucherService.findAll(userId, pageableDto));
    }

    @Operation(summary = "Create voucher")
    @PostMapping
    public ResponseEntity<List<VoucherDto>> createVoucher(@RequestBody @Valid VoucherCreateRequest request) {
        return ResponseEntity.ok(voucherService.create(request));
    }

    @Operation(summary = "Get voucher by ID")
    @GetMapping("/{id}")
    public ResponseEntity<VoucherDto> getVoucherById(@PathVariable("id") String id) {
        return ResponseEntity.ok(voucherService.requireById(id));
    }

    @Operation(summary = "Update voucher")
    @PutMapping("/{id}")
    public ResponseEntity<VoucherDto> updateVoucher(@PathVariable("id") String id,
            @RequestBody @Valid VoucherUpdateRequest request) {
        return ResponseEntity.ok(voucherService.update(id, request));
    }

    @Operation(summary = "Revoke voucher")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeVoucher(@PathVariable("id") String id) {
        voucherService.revoke(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get my vouchers")
    @GetMapping("/my-vouchers")
    public ResponseEntity<List<VoucherDto>> getMyVouchers() {
        return ResponseEntity.ok(voucherService.findMyVouchers());
    }
}