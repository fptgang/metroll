package com.fpt.metroll.shared.domain.dto.ticket;

import com.fpt.metroll.shared.domain.enums.ValidationType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketValidationDto {
    private String id;
    private String stationId;
    private String ticketId;
    private ValidationType validationType;
    private Instant validationTime;
    private String validatorId;
    private Instant createdAt;
}