package com.fpt.metroll.subway.domain.mapper;


import com.fpt.metroll.shared.domain.dto.subway.StationDto;
import com.fpt.metroll.subway.document.Station;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StationMapper {

    @Mapping(source = "location.lat", target = "lat")
    @Mapping(source = "location.lng", target = "lng")
    StationDto toDto(Station station);

    @Mapping(target = "location.lat", source = "lat")
    @Mapping(target = "location.lng", source = "lng")
    Station toEntity(StationDto dto);

    List<StationDto.LineStationInfoDto> toLineStationInfoDtoList(List<Station.LineStationInfo> list);
    List<Station.LineStationInfo> toLineStationInfoList(List<StationDto.LineStationInfoDto> list);

    StationDto.LineStationInfoDto toLineStationInfoDto(Station.LineStationInfo entity);
    Station.LineStationInfo toLineStationInfo(StationDto.LineStationInfoDto dto);
}
