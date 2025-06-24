package com.fpt.metroll.shared.domain.dto.order;

import com.fpt.metroll.shared.domain.enums.TicketType;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutItemRequest {
    private TicketType ticketType; // P2P|TIMED
    private String p2pJourneyId; // P2PJourney ID for P2P tickets
    private String timedTicketPlanId; // TimedTicketPlan ID for TIMED tickets
    private Integer quantity;
} 