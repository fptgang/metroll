package com.fpt.metroll.ticket.repository;

import com.fpt.metroll.ticket.document.TicketValidation;
import com.fpt.metroll.shared.domain.enums.ValidationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketValidationRepository extends MongoRepository<TicketValidation, String> {
    List<TicketValidation> findByTicketIdOrderByValidationTimeDesc(String ticketId);

    List<TicketValidation> findByStationId(String stationId);

    Optional<TicketValidation> findTopByTicketIdAndValidationTypeOrderByValidationTimeDesc(String ticketId,
            ValidationType validationType);
}