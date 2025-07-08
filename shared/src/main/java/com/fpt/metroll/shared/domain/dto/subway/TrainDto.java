package com.fpt.metroll.shared.domain.dto.subway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainDto {
    private String id;
    private String code;
    private String trainNumber;
    private Integer capacity;
    private String status;
    private String assignedLineId;
    private String createdAt;
    private String updatedAt;
}

