package com.fpt.metroll.subway.repository;

import com.fpt.metroll.subway.document.Station;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StationRepository extends MongoRepository<Station, String> {
    Optional<Station> findByCode(String code);

    List<Station> findByCodeIn(Collection<String> codes);
}
