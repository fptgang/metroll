package com.fpt.metroll.shared.service;

import com.fpt.metroll.shared.domain.dto.email.EmailRequest;
import com.fpt.metroll.shared.domain.dto.email.TicketCompensationEmailContext;
import com.fpt.metroll.shared.domain.dto.email.VoucherEmailContext;
import com.fpt.metroll.shared.domain.dto.email.DiscountPackageEmailContext;
import com.fpt.metroll.shared.domain.dto.email.OrderEmailContext;
import com.fpt.metroll.shared.domain.enums.EmailType;

public interface EmailService {

    /**
     * Send a generic email with template
     */
    void sendEmail(EmailRequest emailRequest);

    /**
     * Send voucher-related email (claimed, revoked, expired)
     */
    void sendVoucherEmail(String recipientEmail, String recipientName,
            EmailType emailType, VoucherEmailContext context);

    /**
     * Send discount package-related email (assigned, unassigned, expired)
     */
    void sendDiscountPackageEmail(String recipientEmail, String recipientName,
            EmailType emailType, DiscountPackageEmailContext context);

    /**
     * Send order-related email (checkout success, payment confirmation)
     */
    void sendOrderEmail(String recipientEmail, String recipientName,
            EmailType emailType, OrderEmailContext context);

    /**
     * Test email connectivity
     */
    void sendTestEmail(String recipientEmail);

    void sendTicketCompensationEmail(String recipientEmail, String recipientName, TicketCompensationEmailContext context);
}