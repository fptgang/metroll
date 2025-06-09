package com.fpt.metroll.shared.domain.dto.subway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetroLineQueryParam {

    private String name;
    private String code;

    @Builder.Default
    private String status = "OPERATIONAL";

}
