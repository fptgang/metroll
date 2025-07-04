package com.fpt.metroll.account.service;

import com.fpt.metroll.account.domain.dto.AccountDiscountAssignRequest;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.discount.AccountDiscountPackageDto;

import java.util.List;
import java.util.Optional;

public interface AccountDiscountPackageService {
    PageDto<AccountDiscountPackageDto> findAll(String accountId, String packageId, PageableDto pageable);

    Optional<AccountDiscountPackageDto> findById(String id);

    AccountDiscountPackageDto requireById(String id);

    AccountDiscountPackageDto assign(AccountDiscountAssignRequest request);

    void unassign(String id);

    AccountDiscountPackageDto findMyActivatedDiscounts();
}