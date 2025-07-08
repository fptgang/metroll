package com.fpt.metroll.subway.domain.mapper;

import com.fpt.metroll.subway.document.Train;
import com.fpt.metroll.shared.domain.dto.subway.TrainDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TrainMapper {
    TrainMapper INSTANCE = Mappers.getMapper(TrainMapper.class);
    TrainDto toDto(Train train);
    Train toEntity(TrainDto dto);
}

