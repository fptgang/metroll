package com.fpt.metroll.order.controller;

import com.fpt.metroll.order.service.OrderService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.order.CheckoutRequest;
import com.fpt.metroll.shared.domain.dto.order.OrderDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/")
@Tag(name = "Order", description = "Order API")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "MSS-11: Check out - Create order and process payment")
    @PostMapping("/orders/checkout")
    public ResponseEntity<OrderDto> checkout(@RequestBody @Valid CheckoutRequest checkoutRequest) {
        return ResponseEntity.ok(orderService.checkout(checkoutRequest));
    }

    @Operation(summary = "MSS-13: List my orders - Get current user's orders")
    @GetMapping("/orders/my-orders")
    public ResponseEntity<PageDto<OrderDto>> getMyOrders(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(orderService.getMyOrders(search, pageableDto));
    }

    @Operation(summary = "MSS-14: Read all orders - Get all orders (Admin/Staff only)")
    @GetMapping("/orders")
    public ResponseEntity<PageDto<OrderDto>> getAllOrders(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(orderService.getAllOrders(search, pageableDto));
    }

    @Operation(summary = "MSS-15: Read one order - Get order details by ID")
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable("orderId") String orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
