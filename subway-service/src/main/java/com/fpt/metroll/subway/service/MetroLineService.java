package com.fpt.metroll.subway.service;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.MetroLineDto;
import com.fpt.metroll.shared.domain.dto.subway.MetroLineQueryParam;
import com.fpt.metroll.subway.domain.dto.MetroLineRequest;

public interface MetroLineService {

    MetroLineDto getMetroLineByCode(String lineCode);

    MetroLineDto save(MetroLineRequest request);
    MetroLineDto update(String id, MetroLineRequest request);

    PageDto<MetroLineDto> list(MetroLineQueryParam queryParam, PageableDto pageable);

}
