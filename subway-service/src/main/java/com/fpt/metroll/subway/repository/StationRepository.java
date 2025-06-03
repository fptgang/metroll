package com.fpt.metroll.subway.repository;

import com.fpt.metroll.subway.document.Station;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StationRepository extends MongoRepository<Station, String> {

}
