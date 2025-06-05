package com.fpt.metroll.ticket.document;

import com.fpt.metroll.shared.domain.enums.ValidationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "ticket_validations")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketValidation {

    @Id
    private String id;

    private String stationId; // Reference to Station in subway-service
    private String ticketId; // Reference to Ticket
    private ValidationType validationType;
    private Instant validationTime;
    private String deviceId; // ID of the validation device

    @CreatedDate
    private Instant createdAt;
}