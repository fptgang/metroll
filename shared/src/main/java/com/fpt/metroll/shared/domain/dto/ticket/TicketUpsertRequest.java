package com.fpt.metroll.shared.domain.dto.ticket;

import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketUpsertRequest {
    private TicketType ticketType;
    private String ticketNumber;
    private String ticketOrderDetailId;
    private Instant validUntil;
    private TicketStatus status;
}
