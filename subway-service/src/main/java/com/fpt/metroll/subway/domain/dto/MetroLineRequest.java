package com.fpt.metroll.subway.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetroLineRequest {

    private String id;
    private String code;
    private String name;
    private String color;
    private String operatingHours;

    @Builder.Default
    private String status = "PLANNED";
    private String description;

    @Builder.Default
    private List<SegmentRequest> segments = List.of();

}
