package com.fpt.metroll.subway.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SegmentRequest {
    private Integer sequence;
    private Double distance;
    private Integer travelTime;
    private String description;
    private String startStationCode;
    private String endStationCode;
}
