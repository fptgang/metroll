package com.fpt.metroll.shared.domain.dto.subway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationDto {

    private String id;
    private String code;
    private String name;
    private String address;
    private double lat;
    private double lng;
    private String status;
    private String description;
    private List<LineStationInfoDto> lineStationInfos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LineStationInfoDto {
        private String lineId;
        private String code;
        private Integer sequence;
    }

}
