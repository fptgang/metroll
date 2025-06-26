package com.fpt.metroll.ticket.controller;

import com.fpt.metroll.ticket.domain.dto.P2PJourneyCreateRequest;
import com.fpt.metroll.ticket.domain.dto.P2PJourneyUpdateRequest;
import com.fpt.metroll.ticket.service.P2PJourneyService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.P2PJourneyDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/p2p-journeys")
@Tag(name = "P2P Journey", description = "Point-to-Point Journey API")
@SecurityRequirement(name = "bearerAuth")
public class P2PJourneyController {

    private final P2PJourneyService p2pJourneyService;

    public P2PJourneyController(P2PJourneyService p2pJourneyService) {
        this.p2pJourneyService = p2pJourneyService;
    }

    @Operation(summary = "List P2P journeys by search & filter criteria")
    @GetMapping
    public ResponseEntity<PageDto<P2PJourneyDto>> listP2PJourneys(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(p2pJourneyService.findAll(search, pageableDto));
    }

    @Operation(summary = "Create P2P journey")
    @PostMapping
    public ResponseEntity<P2PJourneyDto> createP2PJourney(@RequestBody @Valid P2PJourneyCreateRequest request) {
        return ResponseEntity.ok(p2pJourneyService.create(request));
    }

    @Operation(summary = "Get P2P journey by ID")
    @GetMapping("/{id}")
    public ResponseEntity<P2PJourneyDto> getP2PJourneyById(@PathVariable("id") String id) {
        return ResponseEntity.ok(p2pJourneyService.requireById(id));
    }

    @Operation(summary = "Get P2P journeys by stations")
    @GetMapping("/stations")
    public ResponseEntity<PageDto<P2PJourneyDto>> getP2PJourneyByStations(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(value = "startStationId", required = false) String startStationId,
            @Parameter @RequestParam(value = "endStationId",required = false) String endStationId) {
        return ResponseEntity.ok(p2pJourneyService.findByStations(pageableDto, startStationId, endStationId));
    }

    @Operation(summary = "Update P2P journey")
    @PutMapping("/{id}")
    public ResponseEntity<P2PJourneyDto> updateP2PJourney(@PathVariable("id") String id,
            @RequestBody @Valid P2PJourneyUpdateRequest request) {
        return ResponseEntity.ok(p2pJourneyService.update(id, request));
    }

    @Operation(summary = "Delete P2P journey")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteP2PJourney(@PathVariable("id") String id) {
        p2pJourneyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}