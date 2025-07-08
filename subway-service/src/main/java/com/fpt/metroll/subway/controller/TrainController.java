package com.fpt.metroll.subway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import com.fpt.metroll.subway.document.Train;
import com.fpt.metroll.subway.service.TrainService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.TrainDto;
import com.fpt.metroll.shared.domain.dto.subway.TrainQueryParam;
import org.springdoc.core.annotations.ParameterObject;

@RestController
@RequestMapping("/trains")
public class TrainController {
    private final TrainService trainService;

    @Autowired
    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @PostMapping
    public ResponseEntity<Train> saveTrain(@RequestBody Train train) {
        return ResponseEntity.ok(trainService.save(train));
    }

    @GetMapping
    public ResponseEntity<List<Train>> getAllTrains() {
        return ResponseEntity.ok(trainService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Train> getTrainById(@PathVariable String id) {
        Optional<Train> train = trainService.findById(id);
        return train.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrain(@PathVariable String id) {
        trainService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paged")
    public ResponseEntity<PageDto<TrainDto>> listTrains(
            @ParameterObject TrainQueryParam queryParam,
            @ParameterObject PageableDto pageable
    ) {
        return ResponseEntity.ok(trainService.findAll(queryParam, pageable));
    }

    @PostMapping("/create-list")
    public ResponseEntity<List<Train>> createTrainList(@RequestBody List<Train> trains) {
        List<Train> createdTrains = trains.stream()
                .map(trainService::save)
                .toList();
        return ResponseEntity.ok(createdTrains);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Train> updateTrain(@PathVariable String id, @RequestBody Train train) {
        train.setId(id);
        return ResponseEntity.ok(trainService.save(train));
    }
}
