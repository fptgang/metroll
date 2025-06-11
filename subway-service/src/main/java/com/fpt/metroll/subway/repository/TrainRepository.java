package com.fpt.metroll.subway.repository;

import com.fpt.metroll.subway.document.Train;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TrainRepository extends MongoRepository<Train, String> {

}
