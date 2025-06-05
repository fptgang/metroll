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
public class MetroLineDto {

    private String id;
    private String code;
    private String name;
    private String color;
    private String operatingHours;
    private String status;
    private String description;
    private List<SegmentDto> segments;
}
