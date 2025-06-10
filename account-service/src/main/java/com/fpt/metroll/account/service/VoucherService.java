package com.fpt.metroll.account.service;

import com.fpt.metroll.account.domain.dto.VoucherCreateRequest;
import com.fpt.metroll.account.domain.dto.VoucherUpdateRequest;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.voucher.VoucherDto;

import java.util.List;
import java.util.Optional;

public interface VoucherService {
    PageDto<VoucherDto> findAll(String userId, PageableDto pageable);
    Optional<VoucherDto> findById(String id);
    VoucherDto requireById(String id);
    List<VoucherDto> create(VoucherCreateRequest request);
    VoucherDto update(String id, VoucherUpdateRequest request);
    void revoke(String id);
} 