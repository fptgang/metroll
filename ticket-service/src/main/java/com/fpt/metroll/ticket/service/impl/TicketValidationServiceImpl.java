package com.fpt.metroll.ticket.service.impl;

import com.fpt.metroll.ticket.document.Ticket;
import com.fpt.metroll.ticket.document.TicketValidation;
import com.fpt.metroll.ticket.domain.dto.TicketValidationCreateRequest;
import com.fpt.metroll.ticket.domain.mapper.TicketValidationMapper;
import com.fpt.metroll.ticket.repository.TicketRepository;
import com.fpt.metroll.ticket.repository.TicketValidationRepository;
import com.fpt.metroll.ticket.service.TicketValidationService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketValidationDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import com.fpt.metroll.shared.domain.enums.ValidationType;
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
public class TicketValidationServiceImpl implements TicketValidationService {

    private final MongoHelper mongoHelper;
    private final TicketValidationMapper mapper;
    private final TicketValidationRepository repository;
    private final TicketRepository ticketRepository;

    public TicketValidationServiceImpl(MongoHelper mongoHelper,
            TicketValidationMapper mapper,
            TicketValidationRepository repository,
            TicketRepository ticketRepository) {
        this.mongoHelper = mongoHelper;
        this.mapper = mapper;
        this.repository = repository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public PageDto<TicketValidationDto> findAll(String search, PageableDto pageable) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        var res = mongoHelper.find(query -> {
            if (search != null && !search.isBlank()) {
                Criteria criteria = new Criteria().orOperator(
                        Criteria.where("ticketId").regex(search, "i"),
                        Criteria.where("stationId").regex(search, "i"),
                        Criteria.where("deviceId").regex(search, "i"));
                query.addCriteria(criteria);
            }
            return query;
        }, pageable, TicketValidation.class).map(mapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    public Optional<TicketValidationDto> findById(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    public TicketValidationDto requireById(String id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket validation not found"));
    }

    @Override
    public List<TicketValidationDto> findByTicketId(String ticketId) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        Preconditions.checkNotNull(ticketId, "Ticket ID cannot be null");
        return repository.findByTicketIdOrderByValidationTimeDesc(ticketId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketValidationDto> findByStationId(String stationId) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        Preconditions.checkNotNull(stationId, "Station ID cannot be null");
        return repository.findByStationId(stationId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TicketValidationDto validateTicket(TicketValidationCreateRequest request) {
        // Basic validation
        Preconditions.checkNotNull(request, "Request cannot be null");
        Preconditions.checkArgument(request.getTicketId() != null && !request.getTicketId().isBlank(),
                "Ticket ID cannot be null or blank");
        Preconditions.checkArgument(request.getStationId() != null && !request.getStationId().isBlank(),
                "Station ID cannot be null or blank");
        Preconditions.checkArgument(request.getValidationType() != null,
                "Validation type cannot be null");
        Preconditions.checkArgument(request.getDeviceId() != null && !request.getDeviceId().isBlank(),
                "Device ID cannot be null or blank");

        // Get ticket information
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        // Check ticket validity
        validateTicketStatus(ticket);
        validateTicketExpiry(ticket);

        // Validate based on ticket type
        if (ticket.getTicketType() == TicketType.P2P) {
            validateP2PTicket(ticket, request);
        } else if (ticket.getTicketType() == TicketType.TIMED) {
            validateTimedTicket(ticket, request);
        }

        // Create validation record
        TicketValidation validation = mapper.toDocument(request);
        validation = repository.save(validation);

        // Update ticket status if needed
        updateTicketStatusAfterValidation(ticket, request.getValidationType());

        log.info("Validated ticket: {} at station: {} with type: {}",
                request.getTicketId(), request.getStationId(), request.getValidationType());

        return mapper.toDto(validation);
    }

    private void validateTicketStatus(Ticket ticket) {
        if (ticket.getStatus() != TicketStatus.VALID) {
            throw new IllegalArgumentException("Ticket is not valid for use. Status: " + ticket.getStatus());
        }
    }

    private void validateTicketExpiry(Ticket ticket) {
        if (ticket.getValidUntil() != null && ticket.getValidUntil().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Ticket has expired");
        }
    }

    private void validateP2PTicket(Ticket ticket, TicketValidationCreateRequest request) {
        // For P2P tickets, check validation history
        List<TicketValidation> validations = repository.findByTicketIdOrderByValidationTimeDesc(ticket.getId());

        if (request.getValidationType() == ValidationType.ENTRY) {
            // Check if already has an unmatched entry
            boolean hasUnmatchedEntry = validations.stream()
                    .anyMatch(v -> v.getValidationType() == ValidationType.ENTRY &&
                            validations.stream().noneMatch(exit -> exit.getValidationType() == ValidationType.EXIT &&
                                    exit.getValidationTime().isAfter(v.getValidationTime())));

            if (hasUnmatchedEntry) {
                throw new IllegalArgumentException("Ticket already has an unmatched entry validation");
            }
        } else if (request.getValidationType() == ValidationType.EXIT) {
            // Check if has a matching entry
            boolean hasMatchingEntry = validations.stream()
                    .anyMatch(v -> v.getValidationType() == ValidationType.ENTRY &&
                            validations.stream().noneMatch(exit -> exit.getValidationType() == ValidationType.EXIT &&
                                    exit.getValidationTime().isAfter(v.getValidationTime())));

            if (!hasMatchingEntry) {
                throw new IllegalArgumentException("No matching entry validation found for this exit");
            }
        }
    }

    private void validateTimedTicket(Ticket ticket, TicketValidationCreateRequest request) {
        // For timed tickets, just check if there's an unmatched entry for exit validations
        if (request.getValidationType() == ValidationType.EXIT) {
            List<TicketValidation> validations = repository.findByTicketIdOrderByValidationTimeDesc(ticket.getId());

            boolean hasUnmatchedEntry = validations.stream()
                    .anyMatch(v -> v.getValidationType() == ValidationType.ENTRY &&
                            validations.stream().noneMatch(exit -> exit.getValidationType() == ValidationType.EXIT &&
                                    exit.getValidationTime().isAfter(v.getValidationTime())));

            if (!hasUnmatchedEntry) {
                throw new IllegalArgumentException("No matching entry validation found for this exit");
            }
        }
    }

    private void updateTicketStatusAfterValidation(Ticket ticket, ValidationType validationType) {
        // For P2P tickets, mark as USED after exit validation
        if (ticket.getTicketType() == TicketType.P2P && validationType == ValidationType.EXIT) {
            ticket.setStatus(TicketStatus.USED);
            ticketRepository.save(ticket);
            log.info("Marked P2P ticket as USED: {}", ticket.getId());
        }
        // For timed tickets, status remains VALID until expiry
    }
}