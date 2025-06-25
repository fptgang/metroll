package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.StationDto;
import com.fpt.metroll.shared.domain.dto.subway.StationQueryParam;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "subway-service", configuration = com.fpt.metroll.shared.config.FeignClientConfiguration.class)
public interface SubwayClient {

    @GetMapping("/stations")
    PageDto<StationDto> listStations(
            @ParameterObject StationQueryParam queryParam,
            PageableDto pageable);

    @GetMapping("/stations/{code}")
    StationDto getStationByCode(@PathVariable("code") String stationCode);

    @GetMapping("/lines")
    PageDto<StationDto> listLines(
            @ParameterObject StationQueryParam queryParam,
            PageableDto pageable);

    @GetMapping("/lines/{code}")
    StationDto getLineByCode(@PathVariable("code") String lineCode);
}
