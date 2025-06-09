package com.fpt.metroll.subway.repository;

import com.fpt.metroll.subway.document.MetroLine;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MetroLineRepository extends MongoRepository<MetroLine, String> {

    Optional<MetroLine> findByCode(String code);
}
