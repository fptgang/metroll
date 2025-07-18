package com.fpt.metroll.shared.service.impl;

import com.fpt.metroll.shared.config.EmailConfig;
import com.fpt.metroll.shared.domain.dto.email.EmailRequest;
import com.fpt.metroll.shared.domain.dto.email.VoucherEmailContext;
import com.fpt.metroll.shared.domain.dto.email.DiscountPackageEmailContext;
import com.fpt.metroll.shared.domain.dto.email.OrderEmailContext;
import com.fpt.metroll.shared.domain.enums.EmailType;
import com.fpt.metroll.shared.service.EmailService;
import com.fpt.metroll.shared.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final RestTemplate restTemplate;
    private final SpringTemplateEngine templateEngine;
    private final EmailConfig emailConfig;
    private final String RESEND_API_URL = "https://api.resend.com/emails";

    @Autowired
    public EmailServiceImpl(SpringTemplateEngine templateEngine, EmailConfig emailConfig) {
        this.restTemplate = new RestTemplate();
        this.templateEngine = templateEngine;
        this.emailConfig = emailConfig;
    }

    @Override
    public void sendEmail(EmailRequest emailRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(emailConfig.getApiKey());

            Map<String, Object> emailData = new HashMap<>();
            emailData.put("from", emailConfig.getFromName() + " <" + emailConfig.getFromEmail() + ">");
            emailData.put("to", new String[] { emailRequest.getRecipientEmail() });
            emailData.put("subject", emailRequest.getSubject());

            if (emailRequest.isHtml() && emailRequest.getTemplateName() != null) {
                Context context = new Context();
                if (emailRequest.getTemplateVariables() != null) {
                    context.setVariables(emailRequest.getTemplateVariables());
                }
                String htmlContent = templateEngine.process(emailRequest.getTemplateName(), context);
                emailData.put("html", htmlContent);
            } else {
                emailData.put("text", emailRequest.getSubject());
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    RESEND_API_URL,
                    HttpMethod.POST,
                    request,
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String emailId = (String) response.getBody().get("id");
                log.info("Email sent successfully to {} with ID: {}", emailRequest.getRecipientEmail(), emailId);
            } else {
                throw new RuntimeException("Failed to send email: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", emailRequest.getRecipientEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendVoucherEmail(String recipientEmail, String recipientName,
            EmailType emailType, VoucherEmailContext context) {
        try {
            String subject = getVoucherEmailSubject(emailType, context);
            String templateName = getVoucherEmailTemplate(emailType);

            Map<String, Object> variables = new HashMap<>();
            variables.put("recipientName", recipientName);
            variables.put("voucherCode", context.getVoucherCode());
            variables.put("discountAmount", context.getDiscountAmount());
            variables.put("minTransactionAmount", context.getMinTransactionAmount());
            variables.put("validFrom",
                    context.getValidFrom() != null ? DateTimeUtil.fromInstantToOffset(context.getValidFrom()) : "N/A");
            variables.put("validUntil",
                    context.getValidUntil() != null ? DateTimeUtil.fromInstantToOffset(context.getValidUntil())
                            : "N/A");
            variables.put("status", context.getStatus());
            variables.put("actionPerformedBy", context.getActionPerformedBy());
            variables.put("actionDate",
                    context.getActionDate() != null ? DateTimeUtil.fromInstantToOffset(context.getActionDate())
                            : "N/A");
            variables.put("actionReason", context.getActionReason());
            variables.put("currentYear", java.time.Year.now().getValue());

            EmailRequest emailRequest = EmailRequest.builder()
                    .recipientEmail(recipientEmail)
                    .recipientName(recipientName)
                    .subject(subject)
                    .templateName(templateName)
                    .templateVariables(variables)
                    .isHtml(true)
                    .build();

            sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("Failed to send voucher email to {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendDiscountPackageEmail(String recipientEmail, String recipientName,
            EmailType emailType, DiscountPackageEmailContext context) {
        try {
            String subject = getDiscountPackageEmailSubject(emailType, context);
            String templateName = getDiscountPackageEmailTemplate(emailType);

            Map<String, Object> variables = new HashMap<>();
            variables.put("recipientName", recipientName);
            variables.put("discountPackageName", context.getDiscountPackageName());
            variables.put("discountPackageDescription", context.getDiscountPackageDescription());
            variables.put("discountPercentage", context.getDiscountPercentage());
            variables.put("validFrom",
                    context.getValidFrom() != null ? DateTimeUtil.fromInstantToOffset(context.getValidFrom()) : "N/A");
            variables.put("validUntil",
                    context.getValidUntil() != null ? DateTimeUtil.fromInstantToOffset(context.getValidUntil())
                            : "N/A");
            variables.put("status", context.getStatus());
            variables.put("actionPerformedBy", context.getActionPerformedBy());
            variables.put("actionDate",
                    context.getActionDate() != null ? DateTimeUtil.fromInstantToOffset(context.getActionDate())
                            : "N/A");
            variables.put("actionReason", context.getActionReason());
            variables.put("currentYear", java.time.Year.now().getValue());

            EmailRequest emailRequest = EmailRequest.builder()
                    .recipientEmail(recipientEmail)
                    .recipientName(recipientName)
                    .subject(subject)
                    .templateName(templateName)
                    .templateVariables(variables)
                    .isHtml(true)
                    .build();

            sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("Failed to send discount package email to {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendOrderEmail(String recipientEmail, String recipientName,
            EmailType emailType, OrderEmailContext context) {
        try {
            String subject = getOrderEmailSubject(emailType, context);
            String templateName = getOrderEmailTemplate(emailType);

            Map<String, Object> variables = new HashMap<>();
            variables.put("recipientName", recipientName);
            variables.put("orderId", context.getOrderId());
            variables.put("transactionReference", context.getTransactionReference());
            variables.put("baseTotal", context.getBaseTotal());
            variables.put("discountTotal", context.getDiscountTotal());
            variables.put("finalTotal", context.getFinalTotal());
            variables.put("paymentMethod", context.getPaymentMethod());
            variables.put("status", context.getStatus());
            variables.put("orderDate",
                    context.getOrderDate() != null ? DateTimeUtil.fromInstantToOffset(context.getOrderDate()) : "N/A");
            variables.put("orderItems", context.getOrderItems());
            variables.put("voucherCode", context.getVoucherCode());
            variables.put("discountPackageName", context.getDiscountPackageName());
            variables.put("staffName", context.getStaffName());
            variables.put("paymentUrl", context.getPaymentUrl());
            variables.put("currentYear", java.time.Year.now().getValue());

            EmailRequest emailRequest = EmailRequest.builder()
                    .recipientEmail(recipientEmail)
                    .recipientName(recipientName)
                    .subject(subject)
                    .templateName(templateName)
                    .templateVariables(variables)
                    .isHtml(true)
                    .build();

            sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("Failed to send order email to {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendTestEmail(String recipientEmail) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("recipientEmail", recipientEmail);
            variables.put("currentYear", java.time.Year.now().getValue());

            EmailRequest emailRequest = EmailRequest.builder()
                    .recipientEmail(recipientEmail)
                    .recipientName("Test User")
                    .subject("MeTroll Email Service Test")
                    .templateName("test-email")
                    .templateVariables(variables)
                    .isHtml(true)
                    .build();

            sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("Failed to send test email to {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    // Helper methods for email subjects
    private String getVoucherEmailSubject(EmailType emailType, VoucherEmailContext context) {
        return switch (emailType) {
            case VOUCHER_CLAIMED -> "Your MeTroll Voucher Has Been Claimed";
            case VOUCHER_REVOKED -> "Your MeTroll Voucher Has Been Revoked";
            case VOUCHER_EXPIRED -> "Your MeTroll Voucher Has Expired";
            default -> "MeTroll Voucher Update";
        };
    }

    private String getDiscountPackageEmailSubject(EmailType emailType, DiscountPackageEmailContext context) {
        return switch (emailType) {
            case DISCOUNT_PACKAGE_ASSIGNED -> "You've Been Assigned a MeTroll Discount Package";
            case DISCOUNT_PACKAGE_UNASSIGNED -> "Your MeTroll Discount Package Has Been Removed";
            case DISCOUNT_PACKAGE_EXPIRED -> "Your MeTroll Discount Package Has Expired";
            default -> "MeTroll Discount Package Update";
        };
    }

    private String getOrderEmailSubject(EmailType emailType, OrderEmailContext context) {
        return switch (emailType) {
            case ORDER_CHECKOUT_SUCCESS -> "Order Confirmation - MeTroll";
            case ORDER_PAYMENT_CONFIRMATION -> "Payment Confirmed - MeTroll";
            case ORDER_PAYMENT_FAILED -> "Payment Failed - MeTroll";
            default -> "MeTroll Order Update";
        };
    }

    // Helper methods for email templates
    private String getVoucherEmailTemplate(EmailType emailType) {
        return switch (emailType) {
            case VOUCHER_CLAIMED -> "voucher-claimed";
            case VOUCHER_REVOKED -> "voucher-revoked";
            case VOUCHER_EXPIRED -> "voucher-expired";
            default -> "voucher-generic";
        };
    }

    private String getDiscountPackageEmailTemplate(EmailType emailType) {
        return switch (emailType) {
            case DISCOUNT_PACKAGE_ASSIGNED -> "discount-package-assigned";
            case DISCOUNT_PACKAGE_UNASSIGNED -> "discount-package-unassigned";
            case DISCOUNT_PACKAGE_EXPIRED -> "discount-package-expired";
            default -> "discount-package-generic";
        };
    }

    private String getOrderEmailTemplate(EmailType emailType) {
        return switch (emailType) {
            case ORDER_CHECKOUT_SUCCESS -> "order-checkout-success";
            case ORDER_PAYMENT_CONFIRMATION -> "order-payment-confirmation";
            case ORDER_PAYMENT_FAILED -> "order-payment-failed";
            default -> "order-generic";
        };
    }
}