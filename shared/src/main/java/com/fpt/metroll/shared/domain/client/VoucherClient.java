package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.voucher.VoucherDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "account-service", contextId = "voucherClient", configuration = com.fpt.metroll.shared.config.FeignClientConfiguration.class)
public interface VoucherClient {

    @GetMapping("/vouchers/{voucherId}")
    VoucherDto getVoucher(@PathVariable("voucherId") String voucherId);

    @PutMapping("/vouchers/{voucherId}/use")
    void use(@PathVariable("voucherId") String voucherId);

    @PutMapping("/vouchers/{voucherId}/preserve")
    void preserve(@PathVariable("voucherId") String voucherId,
                  @RequestParam(name = "userId", required = false) String userId);

    @PutMapping("/vouchers/{voucherId}/unpreserve")
    void unpreserve(@PathVariable("voucherId") String voucherId);
}