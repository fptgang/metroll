package com.fpt.metroll.order.service;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.order.CheckoutRequest;
import com.fpt.metroll.shared.domain.dto.order.OrderDto;
import com.fpt.metroll.shared.domain.dto.order.OrderDetailDto;

import java.util.Optional;

public interface OrderService {

    // MSS-11: Check out
    OrderDto checkout(CheckoutRequest checkoutRequest);

    // MSS-13: List my orders
    PageDto<OrderDto> getMyOrders(String search, PageableDto pageable);

    // MSS-14: Read all orders (Admin/Staff)
    PageDto<OrderDto> getAllOrders(String search, PageableDto pageable);

    // MSS-15: Read one order
    Optional<OrderDto> getOrderById(String orderId);

    OrderDto requireOrderById(String orderId);

    // Get order detail by ID
    Optional<OrderDetailDto> getOrderDetailById(String orderDetailId);

    OrderDetailDto requireOrderDetailById(String orderDetailId);
}