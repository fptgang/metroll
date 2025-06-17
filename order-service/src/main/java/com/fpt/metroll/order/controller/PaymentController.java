package com.fpt.metroll.order.controller;

import com.fpt.metroll.order.service.PayOSService;
import com.fpt.metroll.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.WebhookData;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PayOSService payOSService;
    private final OrderService orderService;
    
    @PostMapping("/webhook")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody String webhookBody) {
        try {
            log.info("Received PayOS webhook: {}", webhookBody);
            
            // Verify webhook data
            WebhookData webhookData = payOSService.verifyWebhookData(webhookBody);
            
            if (webhookData != null) {
                // Process payment completion
                payOSService.processPaymentCompletion(webhookData);
                log.info("Successfully processed PayOS webhook for order code: {}", webhookData.getOrderCode());
                return ResponseEntity.ok("OK");
            } else {
                log.warn("Failed to verify PayOS webhook data");
                return ResponseEntity.badRequest().body("Invalid webhook data");
            }
            
        } catch (Exception e) {
            log.error("Error processing PayOS webhook", e);
            return ResponseEntity.internalServerError().body("Error processing webhook");
        }
    }
    
    @GetMapping("/success")
    public ResponseEntity<String> handlePaymentSuccess(@RequestParam String orderId) {
        try {
            log.info("Payment success callback for order: {}", orderId);
            
            // In a real application, you might redirect to a success page
            // For now, just return a simple response
            return ResponseEntity.ok(
                String.format("Payment successful for order %s. You can close this window.", orderId)
            );
            
        } catch (Exception e) {
            log.error("Error handling payment success for order: {}", orderId, e);
            return ResponseEntity.internalServerError().body("Error processing payment success");
        }
    }
    
    @GetMapping("/cancel")
    public ResponseEntity<String> handlePaymentCancel(@RequestParam String orderId) {
        try {
            log.info("Payment cancelled for order: {}", orderId);
            
            // You might want to update the order status to CANCELLED here
            // For now, just return a simple response
            return ResponseEntity.ok(
                String.format("Payment cancelled for order %s. You can close this window.", orderId)
            );
            
        } catch (Exception e) {
            log.error("Error handling payment cancellation for order: {}", orderId, e);
            return ResponseEntity.internalServerError().body("Error processing payment cancellation");
        }
    }
    
    @GetMapping("/status/{orderId}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable String orderId) {
        try {
            // Get order by ID and return payment status
            var order = orderService.getOrderById(orderId);
            
            if (order.isPresent()) {
                return ResponseEntity.ok(order.get().getStatus().toString());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting payment status for order: {}", orderId, e);
            return ResponseEntity.internalServerError().body("Error getting payment status");
        }
    }
} 