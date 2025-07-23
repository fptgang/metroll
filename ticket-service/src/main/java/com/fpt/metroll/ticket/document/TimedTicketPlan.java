package com.fpt.metroll.ticket.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "timed_ticket_plans")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimedTicketPlan {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;
    private Integer validDuration; // Duration in days

    private Double basePrice; // Base price without discounts

    @Builder.Default
    private Boolean isActive = true; // Soft delete flag

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}