package com.fpt.metroll.shared.domain.dto.ticket;

import com.fpt.metroll.shared.domain.enums.ValidationType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class TicketValidationDto {
    private String id;
    private String stationId;
    private String ticketId;
    private ValidationType validationType;
    private Instant validationTime;
    private String deviceId;
    private Instant createdAt;
}