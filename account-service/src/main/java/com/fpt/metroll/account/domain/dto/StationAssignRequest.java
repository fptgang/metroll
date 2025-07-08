package com.fpt.metroll.account.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StationAssignRequest {
    @NotBlank(message = "Station Code is required")
    private String stationCode;
}