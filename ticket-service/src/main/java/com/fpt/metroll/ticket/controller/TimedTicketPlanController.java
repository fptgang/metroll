package com.fpt.metroll.ticket.controller;

import com.fpt.metroll.ticket.domain.dto.TimedTicketPlanCreateRequest;
import com.fpt.metroll.ticket.domain.dto.TimedTicketPlanUpdateRequest;
import com.fpt.metroll.ticket.service.TimedTicketPlanService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.TimedTicketPlanDto;
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
@RequestMapping("/timed-ticket-plans")
@Tag(name = "Timed Ticket Plan", description = "Timed Ticket Plan API")
@SecurityRequirement(name = "bearerAuth")
public class TimedTicketPlanController {

    private final TimedTicketPlanService timedTicketPlanService;

    public TimedTicketPlanController(TimedTicketPlanService timedTicketPlanService) {
        this.timedTicketPlanService = timedTicketPlanService;
    }

    @Operation(summary = "List timed ticket plans by search & filter criteria")
    @GetMapping
    public ResponseEntity<PageDto<TimedTicketPlanDto>> listTimedTicketPlans(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(timedTicketPlanService.findAll(search, pageableDto));
    }

    @Operation(summary = "Create timed ticket plan")
    @PostMapping
    public ResponseEntity<TimedTicketPlanDto> createTimedTicketPlan(
            @RequestBody @Valid TimedTicketPlanCreateRequest request) {
        return ResponseEntity.ok(timedTicketPlanService.create(request));
    }

    @Operation(summary = "Get timed ticket plan by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TimedTicketPlanDto> getTimedTicketPlanById(@PathVariable("id") String id) {
        return ResponseEntity.ok(timedTicketPlanService.requireById(id));
    }

    @Operation(summary = "Update timed ticket plan")
    @PutMapping("/{id}")
    public ResponseEntity<TimedTicketPlanDto> updateTimedTicketPlan(@PathVariable("id") String id,
            @RequestBody @Valid TimedTicketPlanUpdateRequest request) {
        return ResponseEntity.ok(timedTicketPlanService.update(id, request));
    }

    @Operation(summary = "Delete timed ticket plan")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimedTicketPlan(@PathVariable("id") String id) {
        timedTicketPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}