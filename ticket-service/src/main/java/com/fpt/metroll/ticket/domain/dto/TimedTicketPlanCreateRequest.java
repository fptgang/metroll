package com.fpt.metroll.ticket.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimedTicketPlanCreateRequest {
    private String name;
    private Integer validDuration;
    private Double basePrice;
}