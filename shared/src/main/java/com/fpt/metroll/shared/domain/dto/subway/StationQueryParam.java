package com.fpt.metroll.shared.domain.dto.subway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationQueryParam {
    private String name;
    private String code;

    @Builder.Default
    private String status = "OPERATIONAL";

    private String lineCode;
}
