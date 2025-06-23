package com.fpt.metroll.shared.domain.dto.ticket;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class P2PJourneyDto {
    private String id;
    private String startStationId;
    private String endStationId;
    private Double basePrice;
    private Double distance;
    private Integer travelTime;
    private Instant createdAt;
    private Instant updatedAt;
}