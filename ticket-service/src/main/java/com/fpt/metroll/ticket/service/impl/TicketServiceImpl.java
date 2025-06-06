package com.fpt.metroll.ticket.service.impl;

import com.fpt.metroll.shared.domain.dto.ticket.TicketUpsertRequest;
import com.fpt.metroll.ticket.document.Ticket;
import com.fpt.metroll.ticket.domain.mapper.TicketMapper;
import com.fpt.metroll.ticket.repository.TicketRepository;
import com.fpt.metroll.ticket.service.TicketService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TicketServiceImpl implements TicketService {

    private final MongoHelper mongoHelper;
    private final TicketMapper mapper;
    private final TicketRepository repository;

    public TicketServiceImpl(MongoHelper mongoHelper,
            TicketMapper mapper,
            TicketRepository repository) {
        this.mongoHelper = mongoHelper;
        this.mapper = mapper;
        this.repository = repository;
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
    public List<TicketDto> findByOrderDetailId(String orderDetailId) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        Preconditions.checkNotNull(orderDetailId, "Order detail ID cannot be null");
        return repository.findByTicketOrderDetailId(orderDetailId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
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
        log.info("Updated ticket status: {} to {}", id, status);
    }

    @Override
    public TicketDto create(TicketUpsertRequest ticketUpsertRequest) {

        Preconditions.checkNotNull(ticketUpsertRequest, "Ticket DTO cannot be null");
        Preconditions.checkArgument(ticketUpsertRequest.getTicketNumber() != null && !ticketUpsertRequest.getTicketNumber().isBlank(),
                "Ticket number cannot be null or blank");
        Preconditions.checkArgument(ticketUpsertRequest.getTicketOrderDetailId() != null && !ticketUpsertRequest.getTicketOrderDetailId().isBlank(),
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
                .status(TicketStatus.VALID) // Default status
                .build();
        ticket = repository.save(ticket);
        log.info("Created ticket: {}", ticket.getId());
        return mapper.toDto(ticket);
    }

}