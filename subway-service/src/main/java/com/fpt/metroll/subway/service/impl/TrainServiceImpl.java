package com.fpt.metroll.subway.service.impl;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.TrainDto;
import com.fpt.metroll.shared.domain.dto.subway.TrainQueryParam;
import com.fpt.metroll.subway.document.Train;
import com.fpt.metroll.subway.repository.TrainRepository;
import com.fpt.metroll.subway.service.TrainService;
import com.fpt.metroll.subway.domain.mapper.TrainMapper;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.util.MongoHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

@Service
public class TrainServiceImpl implements TrainService {
    private final TrainRepository trainRepository;
    private final MongoHelper mongoHelper;
    private final TrainMapper trainMapper;

    @Autowired
    public TrainServiceImpl(
        TrainRepository trainRepository,
        MongoHelper mongoHelper,
        TrainMapper trainMapper
    ) {
        this.trainRepository = trainRepository;
        this.mongoHelper = mongoHelper;
        this.trainMapper = trainMapper;
    }

    @Override
    public Train save(Train train) {
        return trainRepository.save(train);
    }

    @Override
    public Optional<Train> findById(String id) {
        return trainRepository.findById(id);
    }

    @Override
    public List<Train> findAll() {
        return trainRepository.findAll();
    }

    @Override
    public void deleteById(String id) {
        trainRepository.deleteById(id);
    }

    @Override
    public PageDto<TrainDto> findAll(TrainQueryParam queryParam, PageableDto pageableDto) {
        var result = mongoHelper.find(query -> buildTrainQuery(queryParam), pageableDto, Train.class)
                .map(trainMapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(result);
    }

    private Query buildTrainQuery(TrainQueryParam queryParam) {
        Query query = new Query();
        if (queryParam.getCode() != null && !queryParam.getCode().isEmpty()) {
            query.addCriteria(Criteria.where("code").is(queryParam.getCode()));
        }
        if (queryParam.getTrainNumber() != null && !queryParam.getTrainNumber().isEmpty()) {
            query.addCriteria(Criteria.where("trainNumber").is(queryParam.getTrainNumber()));
        }
        if (queryParam.getStatus() != null && !queryParam.getStatus().isEmpty()) {
            query.addCriteria(Criteria.where("status").is(queryParam.getStatus()));
        }
        if (queryParam.getAssignedLineId() != null && !queryParam.getAssignedLineId().isEmpty()) {
            query.addCriteria(Criteria.where("assignedLineId").is(queryParam.getAssignedLineId()));
        }
        return query;
    }
}
