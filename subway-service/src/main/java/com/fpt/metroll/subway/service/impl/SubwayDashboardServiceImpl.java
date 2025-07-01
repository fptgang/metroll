package com.fpt.metroll.subway.service.impl;

import com.fpt.metroll.subway.domain.dto.SubwayDashboardDto;
import com.fpt.metroll.subway.repository.MetroLineRepository;
import com.fpt.metroll.subway.repository.StationRepository;
import com.fpt.metroll.subway.repository.TrainRepository;
import com.fpt.metroll.subway.service.SubwayDashboardService;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SubwayDashboardServiceImpl implements SubwayDashboardService {

    private final StationRepository stationRepository;
    private final MetroLineRepository metroLineRepository;
    private final TrainRepository trainRepository;

    public SubwayDashboardServiceImpl(StationRepository stationRepository,
            MetroLineRepository metroLineRepository,
            TrainRepository trainRepository) {
        this.stationRepository = stationRepository;
        this.metroLineRepository = metroLineRepository;
        this.trainRepository = trainRepository;
    }

    @Override
    public SubwayDashboardDto getDashboard() {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        // Get basic counts
        long totalStations = stationRepository.count();
        long totalMetroLines = metroLineRepository.count();
        long totalTrains = trainRepository.count();

        // Get all entities for detailed analysis
        var allStations = stationRepository.findAll();
        var allMetroLines = metroLineRepository.findAll();
        var allTrains = trainRepository.findAll();

        // Count stations by metro line
        Map<String, Long> stationsByMetroLine = new HashMap<>();
        for (var metroLine : allMetroLines) {
            long stationCount = allStations.stream()
                    .filter(station -> station.getLineStationInfos() != null &&
                            station.getLineStationInfos().stream()
                                    .anyMatch(info -> metroLine.getCode().equals(info.getLineCode())))
                    .count();
            stationsByMetroLine.put(metroLine.getName(), stationCount);
        }

        // Count trains by metro line
        Map<String, Long> trainsByMetroLine = new HashMap<>();
        for (var metroLine : allMetroLines) {
            long trainCount = allTrains.stream()
                    .filter(train -> train.getAssignedLineId() != null &&
                            train.getAssignedLineId().equals(metroLine.getId()))
                    .count();
            trainsByMetroLine.put(metroLine.getName(), trainCount);
        }

        // Calculate averages
        double averageStationsPerLine = totalMetroLines > 0 ? (double) totalStations / totalMetroLines : 0.0;
        double averageTrainsPerLine = totalMetroLines > 0 ? (double) totalTrains / totalMetroLines : 0.0;

        return SubwayDashboardDto.builder()
                .totalStations(totalStations)
                .totalMetroLines(totalMetroLines)
                .totalTrains(totalTrains)
                .stationsByMetroLine(stationsByMetroLine)
                .trainsByMetroLine(trainsByMetroLine)
                .averageStationsPerLine(Math.round(averageStationsPerLine * 100.0) / 100.0)
                .averageTrainsPerLine(Math.round(averageTrainsPerLine * 100.0) / 100.0)
                .lastUpdated(Instant.now())
                .build();
    }
}