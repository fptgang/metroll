package com.fpt.metroll.shared.domain.dto.firebase;

import com.fpt.metroll.shared.domain.enums.FirebaseTicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketStatusFirebaseDto {

    private String ticketId;
    private TicketType ticketType;
    private FirebaseTicketStatus status;
    private Instant validUntil;

    // P2P ticket journey information
    private String startStationId;
    private String endStationId;
}