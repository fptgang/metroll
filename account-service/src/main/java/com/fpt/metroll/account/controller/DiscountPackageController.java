package com.fpt.metroll.account.controller;

import com.fpt.metroll.account.domain.dto.DiscountPackageCreateRequest;
import com.fpt.metroll.account.domain.dto.DiscountPackageUpdateRequest;
import com.fpt.metroll.account.service.DiscountPackageService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.discount.DiscountPackageDto;
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
@RequestMapping("/discount-packages")
@Tag(name = "Discount Package", description = "Discount Package API")
@SecurityRequirement(name = "bearerAuth")
public class DiscountPackageController {

    private final DiscountPackageService discountPackageService;

    public DiscountPackageController(DiscountPackageService discountPackageService) {
        this.discountPackageService = discountPackageService;
    }

    @Operation(summary = "List discount packages by search & filter criteria")
    @GetMapping
    public ResponseEntity<PageDto<DiscountPackageDto>> listDiscountPackages(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(discountPackageService.findAll(search, pageableDto));
    }

    @Operation(summary = "Create discount package")
    @PostMapping
    public ResponseEntity<DiscountPackageDto> createDiscountPackage(@RequestBody @Valid DiscountPackageCreateRequest request) {
        return ResponseEntity.ok(discountPackageService.create(request));
    }

    @Operation(summary = "Get discount package by ID")
    @GetMapping("/{id}")
    public ResponseEntity<DiscountPackageDto> getDiscountPackageById(@PathVariable("id") String id) {
        return ResponseEntity.ok(discountPackageService.requireById(id));
    }

    @Operation(summary = "Update discount package")
    @PutMapping("/{id}")
    public ResponseEntity<DiscountPackageDto> updateDiscountPackage(@PathVariable("id") String id,
                                                                   @RequestBody @Valid DiscountPackageUpdateRequest request) {
        return ResponseEntity.ok(discountPackageService.update(id, request));
    }

    @Operation(summary = "Terminate discount package")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> terminateDiscountPackage(@PathVariable("id") String id) {
        discountPackageService.terminate(id);
        return ResponseEntity.noContent().build();
    }
} 