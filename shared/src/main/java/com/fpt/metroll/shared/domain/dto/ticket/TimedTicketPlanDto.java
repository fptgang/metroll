package com.fpt.metroll.shared.domain.dto.ticket;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class TimedTicketPlanDto {
    private String id;
    private String name;
    private Integer validDuration;
    private Double basePrice;
    private Instant createdAt;
    private Instant updatedAt;
}