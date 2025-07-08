package com.fpt.metroll.subway.service;

import com.fpt.metroll.subway.document.Train;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.TrainDto;
import com.fpt.metroll.shared.domain.dto.subway.TrainQueryParam;
import java.util.List;
import java.util.Optional;

public interface TrainService {
    Train save(Train train);
    Optional<Train> findById(String id);
    List<Train> findAll();
    void deleteById(String id);
    PageDto<TrainDto> findAll(TrainQueryParam queryParam, PageableDto pageable);
}
