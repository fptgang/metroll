package com.fpt.metroll.subway.controller;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.StationDto;
import com.fpt.metroll.shared.domain.dto.subway.StationQueryParam;
import com.fpt.metroll.subway.service.StationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("stations/v1")
public class StationController {

    private StationService stationService;

    @Autowired
    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping
    @Operation(summary = "Create or update a station")
    public ResponseEntity<StationDto> saveStation(
            @RequestBody StationDto stationDto
    ) {
        return ResponseEntity.ok(stationService.save(stationDto));
    }

    @PostMapping("/list")
    @Operation(summary = "List stations with query parameters")
    public ResponseEntity<PageDto<StationDto>> listStations(
            @RequestBody StationQueryParam queryParam,
            @ParameterObject PageableDto pageable
    ) {
        return ResponseEntity.ok(stationService.findAll(queryParam, pageable));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get station by code")
    public ResponseEntity<StationDto> getStationByCode(
            @PathVariable("code") String stationCode
    ) {
        return ResponseEntity.ok(stationService.getStationByCode(stationCode));
    }

    @PostMapping("create-list")
    @Operation(summary = "Create a list of stations")
    public ResponseEntity<List<StationDto>> createStationList(
            @RequestBody List<StationDto> stationDtos
    ) {
        List<StationDto> createdStations = stationDtos.stream()
                .map(stationService::save)
                .toList();
        return ResponseEntity.ok(createdStations);

    }
}
