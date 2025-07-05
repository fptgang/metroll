package com.fpt.metroll.ticket.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.metroll.shared.domain.client.OrderClient;
import com.fpt.metroll.shared.domain.dto.order.OrderDetailDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketUpsertRequest;
import com.fpt.metroll.shared.domain.enums.TicketType;
import com.fpt.metroll.ticket.document.P2PJourney;
import com.fpt.metroll.ticket.document.Ticket;
import com.fpt.metroll.ticket.domain.mapper.TicketMapper;
import com.fpt.metroll.ticket.repository.P2PJourneyRepository;
import com.fpt.metroll.ticket.repository.TicketRepository;
import com.fpt.metroll.ticket.service.TicketService;
import com.fpt.metroll.ticket.service.FirebaseTicketStatusService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.google.common.base.Preconditions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TicketServiceImpl implements TicketService {
    
    private static final String TICKETS_PATH = "tickets";

    private final MongoHelper mongoHelper;
    private final TicketMapper mapper;
    private final TicketRepository repository;
    private final FirebaseTicketStatusService firebaseTicketStatusService;
    private final OrderClient orderClient;
    private final P2PJourneyRepository p2PJourneyRepository;
    private final DatabaseReference database;

    public TicketServiceImpl(MongoHelper mongoHelper,
                             TicketMapper mapper,
                             TicketRepository repository,
                             FirebaseTicketStatusService firebaseTicketStatusService, OrderClient orderClient, P2PJourneyRepository p2PJourneyRepository) {
        this.mongoHelper = mongoHelper;
        this.database = FirebaseDatabase.getInstance().getReference();
        this.mapper = mapper;
        this.repository = repository;
        this.firebaseTicketStatusService = firebaseTicketStatusService;
        this.orderClient = orderClient;
        this.p2PJourneyRepository = p2PJourneyRepository;
    }

    @Override
    public PageDto<TicketDto> findAll(String search, PageableDto pageable) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        var res = mongoHelper.find(query -> {
            if (search != null && !search.isBlank()) {
                Criteria criteria = new Criteria().orOperator(
                        Criteria.where("ticketNumber").regex(search, "i"),
                        Criteria.where("ticketOrderDetailId").regex(search, "i"));
                query.addCriteria(criteria);
            }
            return query;
        }, pageable, Ticket.class).map(mapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    public Optional<TicketDto> findById(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public TicketDto requireById(String id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

    @Override
    public Optional<TicketDto> findByTicketNumber(String ticketNumber) {
        Preconditions.checkNotNull(ticketNumber, "Ticket number cannot be null");
        return repository.findByTicketNumber(ticketNumber).map(mapper::toDto);
    }

    @Override
    public TicketDto requireByTicketNumber(String ticketNumber) {
        return findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

    @Override
    public TicketDto findByOrderDetailId(String orderDetailId) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        Preconditions.checkNotNull(orderDetailId, "Order detail ID cannot be null");
        return mapper.toDto(repository.findByTicketOrderDetailId(orderDetailId).orElseThrow(
                () -> new IllegalArgumentException("Ticket not found")));
    }

    @Override
    public List<TicketDto> findByStatus(TicketStatus status) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        Preconditions.checkNotNull(status, "Status cannot be null");
        return repository.findByStatus(status)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(String id, TicketStatus status) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");
        Preconditions.checkNotNull(status, "Status cannot be null");

        Ticket ticket = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        ticket.setStatus(status);
        repository.save(ticket);

        // Update Firebase ticket status
        firebaseTicketStatusService.updateTicketStatusAfterValidation(
                ticket.getId(),
                ticket.getTicketType(),
                ticket.getStatus());

        log.info("Updated ticket status: {} to {}", id, status);
    }

    @Override
    public TicketDto create(TicketUpsertRequest ticketUpsertRequest) {

        Preconditions.checkNotNull(ticketUpsertRequest, "Ticket DTO cannot be null");
        Preconditions.checkArgument(
                ticketUpsertRequest.getTicketNumber() != null && !ticketUpsertRequest.getTicketNumber().isBlank(),
                "Ticket number cannot be null or blank");
        Preconditions.checkArgument(
                ticketUpsertRequest.getTicketOrderDetailId() != null
                        && !ticketUpsertRequest.getTicketOrderDetailId().isBlank(),
                "Order detail ID cannot be null or blank");

        // Check if ticket already exists
        if (repository.findByTicketNumber(ticketUpsertRequest.getTicketNumber()).isPresent()) {
            throw new IllegalArgumentException("Ticket with this number already exists");
        }

        Ticket ticket = Ticket.builder()
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .ticketType(ticketUpsertRequest.getTicketType())
                .ticketNumber(ticketUpsertRequest.getTicketNumber())
                .ticketOrderDetailId(ticketUpsertRequest.getTicketOrderDetailId())
                .purchaseDate(Instant.now())
                .validUntil(ticketUpsertRequest.getValidUntil())
                .status(ticketUpsertRequest.getStatus() != null ? ticketUpsertRequest.getStatus() : TicketStatus.VALID)
                .build();
        ticket = repository.save(ticket);
        log.info("Created ticket: {}", ticket.getId());

        // Create ticket status in Firebase
        firebaseTicketStatusService.createTicketStatus(
                ticket.getId(),
                ticket.getTicketType(),
                ticket.getStatus(),
                ticket.getValidUntil(),
                ticket.getTicketOrderDetailId());

        return mapper.toDto(ticket);
    }

    @Override
    public List<TicketDto> createTickets(List<TicketUpsertRequest> ticketRequests) {
        Preconditions.checkNotNull(ticketRequests, "Ticket requests cannot be null");
        Preconditions.checkArgument(!ticketRequests.isEmpty(), "Ticket requests cannot be empty");

        List<Ticket> tickets = new ArrayList<>();

        for (TicketUpsertRequest request : ticketRequests) {
            Preconditions.checkNotNull(request, "Ticket request cannot be null");
            Preconditions.checkArgument(
                    request.getTicketOrderDetailId() != null && !request.getTicketOrderDetailId().isBlank(),
                    "Order detail ID cannot be null or blank");

            // Generate unique ticket number if not provided
            String ticketNumber = request.getTicketNumber();
            if (ticketNumber == null || ticketNumber.isBlank()) {
                ticketNumber = generateTicketNumber();
            }

            // Check if ticket number already exists
            if (repository.findByTicketNumber(ticketNumber).isPresent()) {
                throw new IllegalArgumentException("Ticket with number " + ticketNumber + " already exists");
            }

            Ticket ticket = Ticket.builder()
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .ticketType(request.getTicketType())
                    .ticketNumber(ticketNumber)
                    .ticketOrderDetailId(request.getTicketOrderDetailId())
                    .purchaseDate(Instant.now())
                    .validUntil(request.getValidUntil())
                    .status(request.getStatus() != null ? request.getStatus() : TicketStatus.VALID)
                    .build();

            tickets.add(ticket);
        }

        // Save all tickets in batch
        List<Ticket> savedTickets = repository.saveAll(tickets);
        log.info("Created {} tickets in batch", savedTickets.size());

        // Create Firebase entries for all tickets
        for (Ticket savedTicket : savedTickets) {
            firebaseTicketStatusService.createTicketStatus(
                    savedTicket.getId(),
                    savedTicket.getTicketType(),
                    savedTicket.getStatus(),
                    savedTicket.getValidUntil(),
                    savedTicket.getTicketOrderDetailId());
        }

        return savedTickets.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    private String generateTicketNumber() {
        return "TKT-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    public String generateQRCodeBase64(String id) throws Exception {
        Ticket ticket = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if(ticket.getStatus() != TicketStatus.VALID) {
            throw new IllegalArgumentException("Ticket is not valid for use. Status: " + ticket.getStatus());
        }

        // Configure ObjectMapper to handle Java 8 date/time types
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        String content = objectMapper.writeValueAsString(ticket);
        int width = 300;
        int height = 300;
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
        
        // Update Firebase ticket status
        
        Map<String, Object> ticketData = new HashMap<>();
        if (ticket.getTicketType() == TicketType.P2P && ticket.getTicketOrderDetailId() != null) {
            try {
                OrderDetailDto orderDetail = orderClient.getOrderDetail(ticket.getTicketOrderDetailId());
                if (orderDetail.getP2pJourney() != null) {
                    P2PJourney p2pJourney = p2PJourneyRepository.findById(orderDetail.getP2pJourney())
                            .orElse(null);
                    if (p2pJourney != null) {
                        ticketData.put("startStationId", p2pJourney.getStartStationId());
                        ticketData.put("endStationId", p2pJourney.getEndStationId());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch P2P journey info for ticket: {}", ticket.getId(), e);
            }
        }
        database.child(TICKETS_PATH).child(ticket.getId()).updateChildrenAsync(ticketData);

        return Base64.getEncoder().encodeToString(os.toByteArray());
    }

    /**
     * Scheduled job to expire tickets whose validUntil is before now and status is
     * VALID
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000) // every 10 minutes
    public void expireTickets() {
        List<Ticket> toExpire = repository.findByStatusAndValidUntilBefore(TicketStatus.VALID, Instant.now());
        if (toExpire.isEmpty()) {
            log.debug("No tickets to expire in this run.");
            return;
        }
        for (Ticket ticket : toExpire) {
            ticket.setStatus(TicketStatus.EXPIRED);
        }
        repository.saveAll(toExpire);
        log.info("Expired {} tickets in scheduled job.", toExpire.size());
    }

}