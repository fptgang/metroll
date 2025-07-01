package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.discount.DiscountPackageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service", contextId = "discountPackageClient", configuration = com.fpt.metroll.shared.config.FeignClientConfiguration.class)
public interface DiscountPackageClient {

    @GetMapping("/discount-packages/{packageId}")
    DiscountPackageDto getDiscountPackage(@PathVariable("packageId") String packageId);
}
