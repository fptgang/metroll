package com.fpt.metroll.shared.domain.dto.subway;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TrainQueryParam {
    private String code;
    private String trainNumber;
    private String status;
    private String assignedLineId;
}

