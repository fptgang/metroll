package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.voucher.VoucherDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

@FeignClient(name = "account-service", contextId = "voucherClient", configuration = com.fpt.metroll.shared.config.FeignClientConfiguration.class)
public interface VoucherClient {

    @GetMapping("/vouchers/{voucherId}")
    VoucherDto getVoucher(@PathVariable("voucherId") String voucherId);

    @GetMapping("/vouchers/my-vouchers")
    List<VoucherDto> getMyVouchers();

    @PutMapping("/vouchers/{voucherId}/use")
    void use(@PathVariable("voucherId") String voucherId);
}