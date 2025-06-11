package com.fpt.metroll.subway.service;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.StationDto;
import com.fpt.metroll.shared.domain.dto.subway.StationQueryParam;

public interface StationService {

    StationDto getStationByCode(String stationCode);

    PageDto<StationDto> findAll(StationQueryParam queryParam, PageableDto pageable);

    StationDto save(StationDto stationDto);
}
