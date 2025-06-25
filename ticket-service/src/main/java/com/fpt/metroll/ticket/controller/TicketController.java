package com.fpt.metroll.ticket.controller;

import com.fpt.metroll.shared.domain.dto.ticket.TicketUpsertRequest;
import com.fpt.metroll.ticket.service.TicketService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketDto;
import com.fpt.metroll.shared.domain.enums.TicketStatus;
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
@RequestMapping("/tickets")
@Tag(name = "Ticket", description = "Ticket API")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Operation(summary = "List tickets by search & filter criteria")
    @GetMapping
    public ResponseEntity<PageDto<TicketDto>> listTickets(
            @ParameterObject @Valid PageableDto pageableDto,
            @Parameter @RequestParam(name = "search", required = false) String search) {
        return ResponseEntity.ok(ticketService.findAll(search, pageableDto));
    }

    @Operation(summary = "Get ticket by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable("id") String id) {
        return ResponseEntity.ok(ticketService.requireById(id));
    }

    @Operation(summary = "Get ticket by ticket number")
    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<TicketDto> getTicketByNumber(@PathVariable("ticketNumber") String ticketNumber) {
        return ResponseEntity.ok(ticketService.requireByTicketNumber(ticketNumber));
    }

    @Operation(summary = "Get tickets by order detail ID")
    @GetMapping("/order-detail/{orderDetailId}")
    public ResponseEntity<TicketDto> getTicketsByOrderDetailId(
            @PathVariable("orderDetailId") String orderDetailId) {
        return ResponseEntity.ok(ticketService.findByOrderDetailId(orderDetailId));
    }

    @Operation(summary = "Get tickets by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketDto>> getTicketsByStatus(@PathVariable("status") TicketStatus status) {
        return ResponseEntity.ok(ticketService.findByStatus(status));
    }

    @Operation(summary = "Update ticket status")
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateTicketStatus(@PathVariable("id") String id,
            @Parameter @RequestParam("status") TicketStatus status) {
        ticketService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create ticket")
    @PostMapping
    public ResponseEntity<TicketDto> createTicket(@RequestBody @Valid TicketUpsertRequest ticketUpsertRequest) {
        return ResponseEntity.ok(ticketService.create(ticketUpsertRequest));
    }

    @Operation(summary = "Create multiple tickets")
    @PostMapping("/batch")
    public ResponseEntity<List<TicketDto>> createTickets(@RequestBody @Valid List<TicketUpsertRequest> ticketRequests) {
        return ResponseEntity.ok(ticketService.createTickets(ticketRequests));
    }

    @Operation(summary = "Generate QR code base64 ")
    @GetMapping("/{id}/qrcode")
    public ResponseEntity<String> generateQRCodeBase64(@PathVariable("id") String id) throws Exception {
        return ResponseEntity.ok(ticketService.generateQRCodeBase64(id));
    }
}