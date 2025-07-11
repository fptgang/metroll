package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.order.OrderDto;
import com.fpt.metroll.shared.domain.dto.order.OrderDetailDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "order-service", contextId = "orderClient", configuration = com.fpt.metroll.shared.config.FeignClientConfiguration.class)
public interface OrderClient {

    @GetMapping("/orders/{orderId}")
    OrderDto getOrder(@PathVariable("orderId") String orderId);

    @GetMapping("/order-details/{orderDetailId}")
    OrderDetailDto getOrderDetail(@PathVariable("orderDetailId") String orderDetailId);
}