package com.fpt.metroll.ticket.controller;

import com.fpt.metroll.shared.domain.enums.ValidationType;
import com.fpt.metroll.ticket.domain.dto.TicketValidationCreateRequest;
import com.fpt.metroll.ticket.service.TicketValidationService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketValidationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ticket-validations")
@Tag(name = "Ticket Validation", description = "Ticket Validation API")
@SecurityRequirement(name = "bearerAuth")
public class TicketValidationController {

    private final TicketValidationService ticketValidationService;

    public TicketValidationController(TicketValidationService ticketValidationService) {
        this.ticketValidationService = ticketValidationService;
    }

    @Operation(summary = "List ticket validations by search & filter criteria")
    @GetMapping
    public ResponseEntity<PageDto<TicketValidationDto>> listTicketValidations(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(ticketValidationService.findAll(search, pageableDto));
    }

    @Operation(summary = "Validate a ticket (check-in/check-out)")
    @PostMapping("/validate")
    public ResponseEntity<TicketValidationDto> validateTicket(
            @RequestBody @Valid TicketValidationCreateRequest request) {
        return ResponseEntity.ok(ticketValidationService.validateTicket(request));
    }

    @Operation(summary = "Get ticket validation by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TicketValidationDto> getTicketValidationById(@PathVariable("id") String id) {
        return ResponseEntity.ok(ticketValidationService.requireById(id));
    }

    @Operation(summary = "Get ticket validations by ticket ID")
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<TicketValidationDto>> getTicketValidationsByTicketId(
            @PathVariable("ticketId") String ticketId) {
        return ResponseEntity.ok(ticketValidationService.findByTicketId(ticketId));
    }

    @Operation(summary = "Get ticket validations by station Code")
    @GetMapping("/station/{stationCode}")
    public ResponseEntity<PageDto<TicketValidationDto>> getTicketValidationsByStationId(
            @PathVariable("stationCode") String stationCode,
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "search", required = false) String search,
            @Parameter @RequestParam(name = "validationType", required = false) ValidationType validationType,
            @Parameter @RequestParam(name = "startDate", required = false) java.time.Instant startDate,
            @Parameter @RequestParam(name = "endDate", required = false) java.time.Instant endDate) {
        return ResponseEntity
                .ok(ticketValidationService.findByStationId(stationCode, search, validationType, startDate, endDate, pageableDto));
    }
}