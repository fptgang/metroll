package com.fpt.metroll.account.service;

import com.fpt.metroll.account.domain.dto.DiscountPackageCreateRequest;
import com.fpt.metroll.account.domain.dto.DiscountPackageUpdateRequest;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.discount.DiscountPackageDto;

import java.util.Optional;

public interface DiscountPackageService {
    PageDto<DiscountPackageDto> findAll(String search, PageableDto pageable);
    Optional<DiscountPackageDto> findById(String id);
    DiscountPackageDto requireById(String id);
    DiscountPackageDto create(DiscountPackageCreateRequest request);
    DiscountPackageDto update(String id, DiscountPackageUpdateRequest request);
    void terminate(String id);
} 