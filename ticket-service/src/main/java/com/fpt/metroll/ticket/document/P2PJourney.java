package com.fpt.metroll.ticket.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "p2p_journeys")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class P2PJourney {

    @Id
    private String id;

    private String startStationId; // Reference to Station in subway-service
    private String endStationId; // Reference to Station in subway-service

    private Double basePrice; // Base price without discounts
    private Double distance; // Distance in kilometers
    private Integer travelTime; // Travel time in minutes

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}