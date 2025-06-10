package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import com.fpt.metroll.shared.domain.dto.discount.DiscountPackageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service")
public interface DiscountPackageClient {

    @GetMapping("/account/discount-packages/{packageId}")
    DiscountPackageDto getDiscountPackage(@PathVariable("packageId") String packageId);
}
