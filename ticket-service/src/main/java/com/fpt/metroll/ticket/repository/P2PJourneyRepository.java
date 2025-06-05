package com.fpt.metroll.ticket.repository;

import com.fpt.metroll.ticket.document.P2PJourney;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface P2PJourneyRepository extends MongoRepository<P2PJourney, String> {
    Optional<P2PJourney> findByStartStationIdAndEndStationId(String startStationId, String endStationId);

    List<P2PJourney> findByStartStationIdOrEndStationId(String startStationId, String endStationId);
}