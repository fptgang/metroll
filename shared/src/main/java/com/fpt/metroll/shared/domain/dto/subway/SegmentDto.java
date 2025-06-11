package com.fpt.metroll.shared.domain.dto.subway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentDto {

    private Integer sequence;
    private Double distance;
    private Integer travelTime;
    private String description;
    private String lineId;

    private StationDto startStation;
    private StationDto endStation;

    private String startStationCode;
    private Integer startStationSequence;

    private String endStationCode;
    private Integer endStationSequence;

}
