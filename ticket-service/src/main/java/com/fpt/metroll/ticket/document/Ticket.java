package com.fpt.metroll.ticket.document;

import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "tickets")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    @Id
    private String id;

    private TicketType ticketType;

    @Indexed(unique = true)
    private String ticketNumber;

    // Reference to TicketOrderDetail from order-service
    private String ticketOrderDetailId;

    private Instant purchaseDate;
    private Instant validUntil;

    private TicketStatus status;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}