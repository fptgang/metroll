package com.fpt.metroll.shared.domain.dto.ticket;

import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {
    private String id;
    private TicketType ticketType;
    private String ticketNumber;
    private String ticketOrderDetailId;
    private Instant purchaseDate;
    private Instant validUntil;
    private TicketStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}