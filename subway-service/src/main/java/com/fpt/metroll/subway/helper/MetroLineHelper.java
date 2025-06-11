package com.fpt.metroll.subway.helper;

import com.fpt.metroll.shared.domain.dto.subway.MetroLineDto;
import com.fpt.metroll.shared.domain.dto.subway.SegmentDto;
import com.fpt.metroll.subway.document.MetroLine;
import com.fpt.metroll.subway.document.Station;
import com.fpt.metroll.subway.domain.dto.SegmentRequest;
import com.fpt.metroll.subway.domain.mapper.StationMapper;
import com.fpt.metroll.subway.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MetroLineHelper {

    private StationRepository stationRepository;
    private StationMapper stationMapper;

    @Autowired
    public MetroLineHelper(StationRepository stationRepository, StationMapper stationMapper) {
        this.stationRepository = stationRepository;
        this.stationMapper = stationMapper;
    }


    public Map<String, Station> validateAndGetStations(List<SegmentRequest> segments) {
        Set<String> codes = segments.stream()
                .flatMap(seg -> Arrays.stream(new String[]{seg.getStartStationCode(), seg.getEndStationCode()}))
                .collect(Collectors.toSet());

        List<Station> stations = stationRepository.findByCodeIn(codes);
        Map<String, Station> stationMap = stations.stream()
                .collect(Collectors.toMap(Station::getCode, s -> s));

        for (String code : codes) {
            Station s = stationMap.get(code);
            if (s == null) throw new IllegalArgumentException("Station code not found: " + code);
            if (s.getStatus() != Station.StationStatus.OPERATIONAL)
                throw new IllegalArgumentException("Station " + s.getName() + " (" + code + ") is not in operational.");
        }
        return stationMap;
    }

    public List<MetroLine.Segment> toSegments(List<SegmentRequest> requests, Map<String, Station> stationMap) {
        return Optional.ofNullable(requests).orElse(Collections.emptyList())
                .stream()
                .map(seg -> MetroLine.Segment.builder()
                        .sequence(seg.getSequence())
                        .distance(seg.getDistance())
                        .travelTime(seg.getTravelTime())
                        .description(seg.getDescription())
                        .startStationCode(stationMap.get(seg.getStartStationCode()).getId())
                        .endStationCode(stationMap.get(seg.getEndStationCode()).getId())
                        .build())
                .collect(Collectors.toList());
    }

    public MetroLineDto buildMetroLineDto(MetroLine line) {
        Set<String> stationIds = line.getSegments().stream()
                .flatMap(seg -> Arrays.stream(new String[]{seg.getStartStationCode(), seg.getEndStationCode()}))
                .collect(Collectors.toSet());
        Map<String, Station> stationMap = stationRepository.findAllById(stationIds)
                .stream().collect(Collectors.toMap(Station::getId, s -> s));
        List<SegmentDto> segmentDtos = line.getSegments().stream()
                .map(segment -> {
                    Station start = stationMap.get(segment.getStartStationCode());
                    Station end = stationMap.get(segment.getEndStationCode());
                    return SegmentDto.builder()
                            .sequence(segment.getSequence())
                            .distance(segment.getDistance())
                            .travelTime(segment.getTravelTime())
                            .description(segment.getDescription())
                            .lineId(line.getId())
                            .startStation(stationMapper.toDto(start))
                            .endStation(stationMapper.toDto(end))
                            .startStationCode(start.getCode())
                            .startStationSequence(getLineSpecificSequence(start, line.getCode()))
                            .endStationCode(end.getCode())
                            .endStationSequence(getLineSpecificSequence(end, line.getCode()))
                            .build();
                }).collect(Collectors.toList());
        return MetroLineDto.builder()
                .id(line.getId())
                .code(line.getCode())
                .name(line.getName())
                .color(line.getColor())
                .operatingHours(line.getOperatingHours())
                .status(line.getStatus().name())
                .description(line.getDescription())
                .segments(segmentDtos)
                .build();
    }

    private Integer getLineSpecificSequence(Station station, String lineCode) {
        if (station == null || station.getLineStationInfos() == null) return null;
        return station.getLineStationInfos().stream()
                .filter(info -> lineCode.equals(info.getLineCode()))
                .findFirst().map(Station.LineStationInfo::getSequence).orElse(null);
    }

    @Transactional
    public void syncLineStations(MetroLine line, List<SegmentRequest> segments) {
        List<String> stationCodesOrdered = new ArrayList<>();
        for (SegmentRequest seg : segments) {
            if (!stationCodesOrdered.contains(seg.getStartStationCode())) {
                stationCodesOrdered.add(seg.getStartStationCode());
            }
            if (!stationCodesOrdered.contains(seg.getEndStationCode())) {
                stationCodesOrdered.add(seg.getEndStationCode());
            }
        }

        Map<String, Station.LineStationInfo> codeToInfo = new HashMap<>();
        for (int i = 0; i < stationCodesOrdered.size(); i++) {
            String stationCode = stationCodesOrdered.get(i);
            int order = i + 1;
            String lineStationCode = line.getCode() + "-" + String.format("%02d", order);

            codeToInfo.put(stationCode, Station.LineStationInfo.builder()
                    .lineCode(line.getCode())
                    .code(lineStationCode)
                    .sequence(order)
                    .build());
        }

        Set<String> codes = codeToInfo.keySet();
        List<Station> stations = stationRepository.findByCodeIn(codes);

        for (Station station : stations) {
            station.getLineStationInfos().removeIf(info -> line.getCode().equals(info.getLineCode()));
            Station.LineStationInfo newInfo = codeToInfo.get(station.getCode());
            if (newInfo != null) {
                station.getLineStationInfos().add(newInfo);
            }
        }

        stationRepository.saveAll(stations);

    }

}
