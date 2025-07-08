package com.fpt.metroll.ticket.service.impl;

import com.fpt.metroll.shared.domain.dto.firebase.TicketStatusFirebaseDto;
import com.fpt.metroll.shared.domain.enums.FirebaseTicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import com.fpt.metroll.ticket.service.FirebaseTicketStatusService;
import com.fpt.metroll.ticket.document.P2PJourney;
import com.fpt.metroll.ticket.repository.P2PJourneyRepository;
import com.fpt.metroll.shared.domain.client.OrderClient;
import com.fpt.metroll.shared.domain.dto.order.OrderDetailDto;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class FirebaseTicketStatusServiceImpl implements FirebaseTicketStatusService {

    private static final String TICKETS_PATH = "tickets";

    private final DatabaseReference database;
    private final OrderClient orderClient;
    private final P2PJourneyRepository p2PJourneyRepository;

    public FirebaseTicketStatusServiceImpl(OrderClient orderClient, P2PJourneyRepository p2PJourneyRepository) {
        this.database = FirebaseDatabase.getInstance().getReference();
        this.orderClient = orderClient;
        this.p2PJourneyRepository = p2PJourneyRepository;
    }

    @Override
    public void createTicketStatus(String ticketId, TicketType ticketType, TicketStatus status, Instant validUntil,
            String orderDetailId) {
        try {
            FirebaseTicketStatus firebaseStatus = convertToFirebaseStatus(status);

            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("ticketId", ticketId);
            ticketData.put("ticketType", ticketType.name());
            ticketData.put("status", firebaseStatus.name());
            ticketData.put("validUntil", validUntil != null ? validUntil.toString() : null);

            database.child(TICKETS_PATH).child(ticketId).setValueAsync(ticketData);

            log.info("Created ticket status in Firebase: {} with type: {} and status: {}", ticketId, ticketType,
                    firebaseStatus);
        } catch (Exception e) {
            log.error("Failed to create ticket status in Firebase for ticket: {}", ticketId, e);
            // Don't throw exception - Firebase failure shouldn't break ticket creation
        }
    }

    @Override
    public void updateTicketStatusAfterValidation(String ticketId, TicketType ticketType, TicketStatus databaseStatus) {
        try {
            FirebaseTicketStatus firebaseStatus = convertToFirebaseStatusAfterValidation(ticketType, databaseStatus);

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", firebaseStatus.name());

            database.child(TICKETS_PATH).child(ticketId).updateChildrenAsync(updates);

            log.info("Updated ticket status in Firebase: {} to status: {}", ticketId, firebaseStatus);
        } catch (Exception e) {
            log.error("Failed to update ticket status in Firebase for ticket: {}", ticketId, e);
            // Don't throw exception - Firebase failure shouldn't break validation
        }
    }

    /**
     * Convert regular TicketStatus to FirebaseTicketStatus
     */
    private FirebaseTicketStatus convertToFirebaseStatus(TicketStatus status) {
        return switch (status) {
            case VALID -> FirebaseTicketStatus.VALID;
            case USED -> FirebaseTicketStatus.USED;
            case EXPIRED -> FirebaseTicketStatus.EXPIRED;
            case CANCELLED -> FirebaseTicketStatus.CANCELLED;
        };
    }

    /**
     * Convert TicketStatus to FirebaseTicketStatus with special handling for timed
     * tickets
     */
    private FirebaseTicketStatus convertToFirebaseStatusAfterValidation(TicketType ticketType,
            TicketStatus databaseStatus) {
        // if still VALID in database but user has ENTRY validation,
        // set to IN_USED in Firebase
        if (databaseStatus == TicketStatus.VALID) {
            return FirebaseTicketStatus.IN_USED;
        }

        // For all other cases, use regular conversion
        return convertToFirebaseStatus(databaseStatus);
    }

    @Override
    public Optional<TicketStatusFirebaseDto> getTicketStatus(String ticketId) {
        try {
            CompletableFuture<Optional<TicketStatusFirebaseDto>> future = new CompletableFuture<>();

            database.child(TICKETS_PATH).child(ticketId)
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Map<String, Object> data = (Map<String, Object>) snapshot.getValue();
                                TicketStatusFirebaseDto dto = TicketStatusFirebaseDto.builder()
                                        .ticketId((String) data.get("ticketId"))
                                        .ticketType(TicketType.valueOf((String) data.get("ticketType")))
                                        .status(FirebaseTicketStatus.valueOf((String) data.get("status")))
                                        .validUntil(
                                                data.get("validUntil") != null
                                                        ? Instant.parse((String) data.get("validUntil"))
                                                        : null)
                                        .startStationId((String) data.get("startStationId"))
                                        .endStationId((String) data.get("endStationId"))
                                        .build();
                                future.complete(Optional.of(dto));
                            } else {
                                future.complete(Optional.empty());
                            }
                        }

                        @Override
                        public void onCancelled(com.google.firebase.database.DatabaseError error) {
                            log.error("Failed to get ticket status from Firebase for ticket: {}", ticketId,
                                    error.toException());
                            future.complete(Optional.empty());
                        }
                    });

            return future.get();
        } catch (Exception e) {
            log.error("Error getting ticket status from Firebase for ticket: {}", ticketId, e);
            return Optional.empty();
        }
    }

    @Override
    public void removeTicketStatus(String ticketId) {
        try {
            database.child(TICKETS_PATH).child(ticketId).removeValueAsync();
            log.info("Removed ticket status from Firebase: {}", ticketId);
        } catch (Exception e) {
            log.error("Failed to remove ticket status from Firebase for ticket: {}", ticketId, e);
        }
    }
}