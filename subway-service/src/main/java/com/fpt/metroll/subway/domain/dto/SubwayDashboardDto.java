package com.fpt.metroll.subway.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubwayDashboardDto {
    private Long totalStations;
    private Long totalMetroLines;
    private Long totalTrains;
    private Map<String, Long> stationsByMetroLine;
    private Map<String, Long> trainsByMetroLine;
    private Double averageStationsPerLine;
    private Double averageTrainsPerLine;
    private Instant lastUpdated;
}