package com.fpt.metroll.ticket.domain.dto;

import com.fpt.metroll.shared.domain.enums.ValidationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidationCreateRequest {
    private String ticketId;
    private ValidationType validationType;
    private String deviceId;
}