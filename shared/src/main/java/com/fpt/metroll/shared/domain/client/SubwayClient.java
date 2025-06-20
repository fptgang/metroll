package com.fpt.metroll.shared.domain.client;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.StationDto;
import com.fpt.metroll.shared.domain.dto.subway.StationQueryParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "subway-service")
public interface SubwayClient {

    @PostMapping("/stations/v1/list")
    PageDto<StationDto> listStations(
            @RequestBody StationQueryParam queryParam,
            PageableDto pageable);

    @GetMapping("/stations/v1/{code}")
    StationDto getStationByCode(@PathVariable("code") String stationCode);

    @PostMapping("/lines/v1/list")
    PageDto<StationDto> listLines(
            @RequestBody StationQueryParam queryParam,
            PageableDto pageable);

    @GetMapping("/lines/v1/{code}")
    StationDto getLineByCode(@PathVariable("code") String lineCode);
}
