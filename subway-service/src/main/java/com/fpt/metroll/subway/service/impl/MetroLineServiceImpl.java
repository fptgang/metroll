package com.fpt.metroll.subway.service.impl;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.MetroLineDto;
import com.fpt.metroll.shared.domain.dto.subway.MetroLineQueryParam;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.subway.document.MetroLine;
import com.fpt.metroll.subway.document.Station;
import com.fpt.metroll.subway.domain.dto.MetroLineRequest;
import com.fpt.metroll.subway.helper.MetroLineHelper;
import com.fpt.metroll.subway.repository.MetroLineRepository;
import com.fpt.metroll.subway.service.MetroLineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
public class MetroLineServiceImpl implements MetroLineService {

    private final MetroLineRepository metroLineRepository;
    private final MongoHelper mongoHelper;
    private final MetroLineHelper metroLineHelper;

    @Autowired
    public MetroLineServiceImpl(
        MetroLineRepository metroLineRepository,
        MongoHelper mongoHelper,
        MetroLineHelper metroLineHelper
    ) {
        this.metroLineRepository = metroLineRepository;
        this.mongoHelper = mongoHelper;
        this.metroLineHelper = metroLineHelper;
    }

    @Override
    public PageDto<MetroLineDto> list(MetroLineQueryParam queryParam, PageableDto pageable) {

        var result = mongoHelper.find(
            query -> buildMetroLineQuery(queryParam), pageable, MetroLine.class)
            .map(metroLineHelper::buildMetroLineDto);

        return PageMapper.INSTANCE.toPageDTO(result);
    }

    @Override
    public MetroLineDto getMetroLineByCode(String lineCode) {
        return metroLineRepository.findByCode(lineCode)
                .map(metroLineHelper::buildMetroLineDto)
                .orElseThrow(() -> new IllegalArgumentException("Metro line not found with code: " + lineCode));
    }

    @Override
    @Transactional
    public MetroLineDto save(MetroLineRequest request) {
        Map<String, Station> stationMap = metroLineHelper.validateAndGetStations(request.getSegments());

        MetroLine line = new MetroLine();
        line.setId(request.getId());
        line.setCode(request.getCode());
        line.setName(request.getName());
        line.setColor(request.getColor());
        line.setOperatingHours(request.getOperatingHours());
        line.setStatus(MetroLine.LineStatus.valueOf(request.getStatus()));
        line.setDescription(request.getDescription());
        line.setSegments(metroLineHelper.toSegments(request.getSegments(), stationMap));

        MetroLine saved = metroLineRepository.save(line);

        metroLineHelper.syncLineStations(saved, request.getSegments());
        return metroLineHelper.buildMetroLineDto(saved);
    }

    @Override
    @Transactional
    public MetroLineDto update(String id, MetroLineRequest request) {
        MetroLine line = metroLineRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MetroLine not found: " + id));

        if (request.getCode() != null && !request.getCode().isEmpty()) {
            line.setCode(request.getCode());
        }
        if (request.getName() != null && !request.getName().isEmpty()) {
            line.setName(request.getName());
        }
        if (request.getColor() != null && !request.getColor().isEmpty()) {
            line.setColor(request.getColor());
        }
        if (request.getOperatingHours() != null && !request.getOperatingHours().isEmpty()) {
            line.setOperatingHours(request.getOperatingHours());
        }
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            line.setStatus(MetroLine.LineStatus.valueOf(request.getStatus()));
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            line.setDescription(request.getDescription());
        }

        if (request.getSegments() != null && !request.getSegments().isEmpty()) {
            Map<String, Station> stationMap = metroLineHelper.validateAndGetStations(request.getSegments());
            line.setSegments(metroLineHelper.toSegments(request.getSegments(), stationMap));
        }

        MetroLine updated = metroLineRepository.save(line);
        metroLineHelper.syncLineStations(updated, request.getSegments());
        return metroLineHelper.buildMetroLineDto(updated);

    }


    private Query buildMetroLineQuery(MetroLineQueryParam queryParam) {
        Query query = new Query();
        if (queryParam.getCode() != null && !queryParam.getCode().isEmpty()) {
            query.addCriteria(Criteria.where("code").is(queryParam.getCode()));
        }
        if (queryParam.getName() != null && !queryParam.getName().isEmpty()) {
            query.addCriteria(Criteria.where("name").regex(queryParam.getName(), "i"));
        }
        if (queryParam.getStatus() != null && !queryParam.getStatus().isEmpty()) {
            query.addCriteria(Criteria.where("status").is(queryParam.getStatus()));
        }
        return query;
    }
}
