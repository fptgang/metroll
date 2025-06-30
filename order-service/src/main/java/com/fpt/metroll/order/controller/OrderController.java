package com.fpt.metroll.order.controller;

import com.fpt.metroll.order.domain.dto.OrderDashboardDto;
import com.fpt.metroll.order.service.CheckoutSagaOrchestrator;
import com.fpt.metroll.order.service.OrderDashboardService;
import com.fpt.metroll.order.service.OrderService;
import com.fpt.metroll.order.service.PayOSService;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.order.CheckoutRequest;
import com.fpt.metroll.shared.domain.dto.order.OrderDto;
import com.fpt.metroll.shared.domain.dto.order.OrderDetailDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.PaymentLinkData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/")
@Tag(name = "Order", description = "Order API")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

        private final OrderService orderService;
        private final PayOSService payOSService;
        private final CheckoutSagaOrchestrator checkoutSagaOrchestrator;
        private final OrderDashboardService orderDashboardService;

        public OrderController(OrderService orderService,
                        PayOSService payOSService,
                        CheckoutSagaOrchestrator checkoutSagaOrchestrator,
                        OrderDashboardService orderDashboardService) {
                this.orderService = orderService;
                this.payOSService = payOSService;
                this.checkoutSagaOrchestrator = checkoutSagaOrchestrator;
                this.orderDashboardService = orderDashboardService;
        }

        @Operation(summary = "Get order service dashboard statistics")
        @GetMapping("/orders/dashboard")
        public ResponseEntity<OrderDashboardDto> getDashboard() {
                return ResponseEntity.ok(orderDashboardService.getDashboard());
        }

        @Operation(summary = "MSS-11: Check out - Create order and process payment")
        @PostMapping("/orders/checkout")
        public ResponseEntity<OrderDto> checkout(
                        @RequestBody @Valid CheckoutRequest checkoutRequest) {
                return ResponseEntity.ok(orderService.checkout(checkoutRequest));
        }

        @Operation(summary = "MSS-11-SAGA: Async checkout using saga pattern")
        @PostMapping("/orders/checkout-saga")
        public ResponseEntity<Map<String, String>> checkoutSaga(
                        @RequestBody @Valid CheckoutRequest checkoutRequest) {
                String userId = SecurityUtil.requireUserId();
                String sagaId = checkoutSagaOrchestrator.startCheckoutSaga(checkoutRequest, userId);

                return ResponseEntity.accepted()
                                .body(Map.of(
                                                "sagaId", sagaId,
                                                "status", "STARTED",
                                                "message",
                                                "Checkout process initiated. Use /orders/saga/{sagaId}/status to track progress."));
        }

        @Operation(summary = "Get saga status")
        @GetMapping("/orders/saga/{sagaId}/status")
        public ResponseEntity<Map<String, String>> getSagaStatus(
                        @PathVariable("sagaId") String sagaId) {
                String status = checkoutSagaOrchestrator.getSagaStatus(sagaId);
                return ResponseEntity.ok(Map.of(
                                "sagaId", sagaId,
                                "status", status));
        }

        @Operation(summary = "MSS-13: List my orders - Get current user's orders")
        @GetMapping("/orders/my-orders")
        public ResponseEntity<PageDto<OrderDto>> getMyOrders(
                        @ParameterObject @Valid PageableDto pageableDto,
                        @Parameter @RequestParam(name = "search", required = false) String search) {
                return ResponseEntity.ok(orderService.getMyOrders(search,
                                pageableDto));
        }

        @Operation(summary = "MSS-14: Read all orders - Get all orders (Admin/Staff only)")
        @GetMapping("/orders")
        public ResponseEntity<PageDto<OrderDto>> getAllOrders(
                        @ParameterObject @Valid PageableDto pageableDto,
                        @Parameter @RequestParam(name = "search", required = false) String search) {
                return ResponseEntity.ok(orderService.getAllOrders(search,
                                pageableDto));
        }

        @Operation(summary = "MSS-15: Read one order - Get order details by ID")
        @GetMapping("/orders/{orderId}")
        public ResponseEntity<OrderDto> getOrderById(
                        @PathVariable("orderId") String orderId) {
                return orderService.getOrderById(orderId)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound()
                                                .build());
        }

        @Operation(summary = "Get order detail by ID")
        @GetMapping("/order-details/{orderDetailId}")
        public ResponseEntity<OrderDetailDto> getOrderDetailById(
                        @PathVariable("orderDetailId") String orderDetailId) {
                return orderService.getOrderDetailById(orderDetailId)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound()
                                                .build());
        }

        // PayOS Integration Endpoints

        @Operation(summary = "Get payment link information")
        @GetMapping("/orders/payments/{orderCode}")
        public ResponseEntity<PaymentLinkData> getPaymentLinkInformation(
                        @Parameter(description = "PayOS order code") @PathVariable(name = "orderCode") Long orderCode) {

                log.info("Getting PayOS payment information for order: {}",
                                orderCode);

                PaymentLinkData paymentInfo = payOSService.getPaymentLinkInfo(orderCode);
                return ResponseEntity.ok(paymentInfo);
        }

        @Operation(summary = "Cancel payment by order code of PayOS")
        @PostMapping("/orders/payments/{orderCode}/cancel")
        public ResponseEntity<PaymentLinkData> cancelPayment(
                        @Parameter(description = "PayOS order code") @PathVariable(name = "orderCode") Long orderCode,
                        @Parameter(description = "Cancellation reason") @RequestParam(required = false, name = "reason") String reason) {

                log.info("Cancelling PayOS payment for order: {} with reason: {}",
                                orderCode,
                                reason);

                String cancellationReason = reason != null ? reason : "User requested cancellation";
                PaymentLinkData cancellationInfo = payOSService.cancelPaymentLink(orderCode,
                                cancellationReason);
                return ResponseEntity.ok(cancellationInfo);
        }

        @Operation(summary = "Handle PayOS webhook", description = "Endpoint to receive PayOS payment status updates")
        @SecurityRequirements
        @PostMapping("/orders/webhook")
        public ResponseEntity<String> handlePaymentWebhook(
                        @RequestBody Webhook webhookData) {

                if (webhookData.getData().getOrderCode() == 123L) {
                        return ResponseEntity.ok("Test webhook received successfully");
                }

                try {
                        log.info("Processing PayOS webhook for order code: {}",
                                        webhookData.getData().getOrderCode());
                        payOSService.processPaymentCompletion(webhookData);
                        return ResponseEntity.ok("Webhook processed successfully");
                } catch (Exception e) {
                        log.error("Failed to process PayOS webhook: {}",
                                        e.getMessage(),
                                        e);
                        return ResponseEntity.badRequest()
                                        .body("Webhook processing failed: " + e.getMessage());
                }
        }

        @Operation(summary = "Confirm webhook URL with PayOS")
        @PostMapping("/orders/webhook/confirm")
        public ResponseEntity<String> confirmWebhook(
                        @Parameter(description = "Webhook URL to confirm", name = "webhookUrl") @RequestParam(name = "webhookUrl") String webhookUrl) {
                log.info("Confirming PayOS webhook URL: {}",
                                webhookUrl);
                String result = payOSService.confirmWebhook(webhookUrl);
                return ResponseEntity.ok(result);
        }
}
