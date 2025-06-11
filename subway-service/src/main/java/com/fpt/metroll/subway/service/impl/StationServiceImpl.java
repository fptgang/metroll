package com.fpt.metroll.subway.service.impl;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.StationDto;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.subway.document.Station;
import com.fpt.metroll.shared.domain.dto.subway.StationQueryParam;
import com.fpt.metroll.subway.domain.mapper.StationMapper;
import com.fpt.metroll.subway.repository.StationRepository;
import com.fpt.metroll.subway.service.StationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StationServiceImpl implements StationService {

    private StationRepository stationRepository;
    private StationMapper stationMapper;
    private MongoHelper mongoHelper;

    @Autowired
    public StationServiceImpl(
            StationRepository stationRepository,
            StationMapper stationMapper,
            MongoHelper mongoHelper
    ) {
        this.stationRepository = stationRepository;
        this.stationMapper = stationMapper;
        this.mongoHelper = mongoHelper;
    }

    @Override
    public StationDto getStationByCode(String stationCode) {
        return stationRepository.findByCode(stationCode)
                .map(stationMapper::toDto)
                .orElseThrow(() -> {
                    log.error("[StationService] getStationByCode {} not found", stationCode);
                    return new IllegalArgumentException("Station not found");
                });
    }

    @Override
    public PageDto<StationDto> findAll(StationQueryParam queryParam, PageableDto pageable) {
        var result = mongoHelper.find( query -> buildStationQuery(queryParam), pageable, Station.class)
                .map(stationMapper::toDto);

        return PageMapper.INSTANCE.toPageDTO(result);
    }

    @Override
    public StationDto save(StationDto stationDto) {
        Station station = stationMapper.toEntity(stationDto);
        station = stationRepository.save(station);
        log.info("[StationService] Saved station code: {}, station {}", station.getCode(), station);
        return stationMapper.toDto(station);

    }

    private Query buildStationQuery(StationQueryParam queryParam) {
        Query query = new Query();
        if (queryParam.getName() != null && !queryParam.getName().isEmpty()) {
            query.addCriteria(Criteria.where("name").regex(queryParam.getName(), "i"));
        }
        if (queryParam.getCode() != null && !queryParam.getCode().isEmpty()) {
            query.addCriteria(Criteria.where("code").is(queryParam.getCode()));
        }
        if (queryParam.getStatus() != null && !queryParam.getStatus().isEmpty()) {
            query.addCriteria(Criteria.where("status").is(queryParam.getStatus()));
        }
        if (queryParam.getLineCode() != null && !queryParam.getLineCode().isEmpty()) {
            query.addCriteria(Criteria.where("lineStationInfos.code").is(queryParam.getLineCode()));
        }

        return query;
    }
}
