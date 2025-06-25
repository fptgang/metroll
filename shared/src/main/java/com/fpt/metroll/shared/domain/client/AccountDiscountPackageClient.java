package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.discount.AccountDiscountPackageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service", contextId = "accountDiscountPackageClient", configuration = com.fpt.metroll.shared.config.FeignClientConfiguration.class)
public interface AccountDiscountPackageClient {

    @GetMapping("/account/account-discount-packages/{packageId}")
    AccountDiscountPackageDto getAccountDiscountPackage(@PathVariable("packageId") String packageId);
} 