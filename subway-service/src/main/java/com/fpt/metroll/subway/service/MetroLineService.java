package com.fpt.metroll.subway.service;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.subway.MetroLineDto;

public interface MetroLineService {

    PageDto<MetroLineDto> findAll();

}
