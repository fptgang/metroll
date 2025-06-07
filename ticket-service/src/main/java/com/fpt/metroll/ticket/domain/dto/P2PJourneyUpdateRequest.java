package com.fpt.metroll.ticket.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class P2PJourneyUpdateRequest {
    private String startStationId;
    private String endStationId;
    private Double basePrice;
    private Double distance;
    private Integer travelTime;
}