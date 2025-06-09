package com.fpt.metroll.subway.controller;

import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.subway.MetroLineDto;
import com.fpt.metroll.shared.domain.dto.subway.MetroLineQueryParam;
import com.fpt.metroll.subway.domain.dto.MetroLineRequest;
import com.fpt.metroll.subway.service.MetroLineService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("lines/v1")
public class MetroLineController {

    private MetroLineService metroLineService;

    public MetroLineController(MetroLineService metroLineService) {
        this.metroLineService = metroLineService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<MetroLineDto> getLineByCode(String code) {
        return ResponseEntity.ok(metroLineService.getMetroLineByCode(code));
    }

    @PostMapping("/list")
    public ResponseEntity<PageDto<MetroLineDto>> listLines(
        @RequestBody MetroLineQueryParam queryParam,
        @ParameterObject PageableDto pageable
    ) {
        return ResponseEntity.ok(metroLineService.list(queryParam, pageable));
    }

    @PostMapping
    public ResponseEntity<MetroLineDto> saveLine(
        @RequestBody MetroLineRequest request
    ) {
        return ResponseEntity.ok(metroLineService.save(request));
    }

    @PutMapping("/{code}")
    public ResponseEntity<MetroLineDto> updateLine(
        @PathVariable String code,
        @RequestBody MetroLineRequest metroLineDto
    ) {
        return ResponseEntity.ok(metroLineService.update(code, metroLineDto));
    }

}
