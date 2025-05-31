package com.fpt.metroll.shared.domain.dto;

import com.fpt.metroll.shared.util.SortDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageableDto {
    private int page;
    private int size;
    private Map<String, SortDirection> sort;
}
