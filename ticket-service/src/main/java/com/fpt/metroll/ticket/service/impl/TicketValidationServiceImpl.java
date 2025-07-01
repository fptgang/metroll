package com.fpt.metroll.ticket.service.impl;

import com.fpt.metroll.ticket.document.P2PJourney;
import com.fpt.metroll.ticket.document.Ticket;
import com.fpt.metroll.ticket.document.TicketValidation;
import com.fpt.metroll.ticket.domain.dto.TicketValidationCreateRequest;
import com.fpt.metroll.ticket.domain.mapper.TicketValidationMapper;
import com.fpt.metroll.ticket.repository.P2PJourneyRepository;
import com.fpt.metroll.ticket.repository.TicketRepository;
import com.fpt.metroll.ticket.repository.TicketValidationRepository;
import com.fpt.metroll.ticket.service.TicketValidationService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.TicketValidationDto;
import com.fpt.metroll.shared.domain.dto.order.OrderDetailDto;
import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.enums.TicketStatus;
import com.fpt.metroll.shared.domain.enums.TicketType;
import com.fpt.metroll.shared.domain.enums.ValidationType;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.fpt.metroll.shared.domain.client.AccountClient;
import com.fpt.metroll.shared.domain.client.OrderClient;
import com.fpt.metroll.shared.domain.client.TicketClient;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

@Slf4j
@Service
public class TicketValidationServiceImpl implements TicketValidationService {

    private final MongoHelper mongoHelper;
    private final TicketValidationMapper mapper;
    private final TicketValidationRepository repository;
    private final TicketRepository ticketRepository;
    private final AccountClient accountClient;
    private final OrderClient orderClient;
    private final P2PJourneyRepository p2PJourneyRepository;

    public TicketValidationServiceImpl(MongoHelper mongoHelper,
            TicketValidationMapper mapper,
            TicketValidationRepository repository,
            TicketRepository ticketRepository,
            AccountClient accountClient,
            OrderClient orderClient,
            P2PJourneyRepository p2PJourneyRepository) {
        this.mongoHelper = mongoHelper;
        this.mapper = mapper;
        this.repository = repository;
        this.ticketRepository = ticketRepository;
        this.accountClient = accountClient;
        this.orderClient = orderClient;
        this.p2PJourneyRepository = p2PJourneyRepository;
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
                        Criteria.where("validatorId").regex(search, "i"));
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
    @Transactional
    public TicketValidationDto validateTicket(TicketValidationCreateRequest request) {
        // Only STAFF can validate tickets
        if (!SecurityUtil.hasRole(AccountRole.STAFF))
            throw new NoPermissionException();

        // Basic validation
        Preconditions.checkNotNull(request, "Request cannot be null");
        Preconditions.checkArgument(request.getTicketId() != null && !request.getTicketId().isBlank(),
                "Ticket ID cannot be null or blank");
        Preconditions.checkArgument(request.getValidationType() != null,
                "Validation type cannot be null");

        // Get the authenticated staff's assigned station
        String staffId = SecurityUtil.requireUserId();
        AccountDto staffAccount = accountClient.getAccount(staffId);

        if (staffAccount.getAssignedStation() == null || staffAccount.getAssignedStation().isBlank()) {
            throw new IllegalStateException("Staff member is not assigned to any station");
        }

        String stationId = staffAccount.getAssignedStation();

        // Get ticket information
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        // Check ticket validity
        validateTicketStatus(ticket);
        validateTicketExpiry(ticket);

        // Validate based on ticket type
        if (ticket.getTicketType() == TicketType.P2P) {
            validateP2PTicket(ticket, stationId, request.getValidationType());
        } else if (ticket.getTicketType() == TicketType.TIMED) {
            validateTimedTicket(ticket, stationId, request.getValidationType());
        }

        // Create validation record
        TicketValidation validation = TicketValidation.builder()
                .ticketId(request.getTicketId())
                .stationId(stationId)
                .validationType(request.getValidationType())
                .validationTime(Instant.now())
                .validatorId(staffId)
                .build();

        validation = repository.save(validation);

        // Update ticket status if needed
        updateTicketStatusAfterValidation(ticket, request.getValidationType());

        log.info("Validated ticket: {} at station: {} with type: {}",
                request.getTicketId(), stationId, request.getValidationType());

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

    private void validateP2PTicket(Ticket ticket, String stationId, ValidationType validationType) {
        Preconditions.checkArgument(ticket.getTicketOrderDetailId() != null,
                "Ticket must have an associated order detail for P2P validation");
        OrderDetailDto orderDetail = orderClient.getOrderDetail(ticket.getTicketOrderDetailId());

        Preconditions.checkArgument(orderDetail.getP2pJourney() != null,
                "P2P ticket must have an associated journey");
        P2PJourney p2pJourney = p2PJourneyRepository.findById(orderDetail.getP2pJourney()).orElseThrow(
                () -> new IllegalArgumentException("P2P journey not found"));

        log.info("Validating P2P {} -> {} at station: {}",
                p2pJourney.getStartStationId(),
                p2pJourney.getEndStationId(), stationId);

        List<TicketValidation> validations = repository.findByTicketIdOrderByValidationTimeDesc(ticket.getId());

        if (validationType == ValidationType.ENTRY) {
            if (!Objects.equals(stationId, p2pJourney.getStartStationId())) {
                throw new IllegalStateException(
                        String.format("Entry validation must be at start station. Expected: %s, Got: %s",
                                p2pJourney.getStartStationId(), stationId));
            }
            
            // must have no validation before
            if (!validations.isEmpty()) {
                throw new IllegalStateException("The ticket can no longer be validated for ENTRY");
            }
        } else if (validationType == ValidationType.EXIT) {
            if (!Objects.equals(stationId, p2pJourney.getEndStationId())) {
                throw new IllegalArgumentException(
                        String.format("Exit validation must be at end station. Expected: %s, Got: %s",
                                p2pJourney.getEndStationId(), stationId));
            }

            // if no validation before, the ticket must be validated for ENTRY first
            if (validations.isEmpty()) {
                throw new IllegalStateException("The ticket must be validated for ENTRY first");
            }
            // if have validation before, this ticket is no longer usable
            if (validations.size() > 1) {
                throw new IllegalStateException("The ticket can no longer be validated for EXIT");
            }
        }

    }

    private void validateTimedTicket(Ticket ticket, String stationId, ValidationType validationType) {
        List<TicketValidation> validations = repository.findByTicketIdOrderByValidationTimeDesc(ticket.getId());

        if (validations.isEmpty()) {
            // First validation must be ENTRY
            if (validationType != ValidationType.ENTRY) {
                throw new IllegalStateException("First validation for timed ticket must be ENTRY");
            }
        } else {
            // Check alternating pattern: ENTRY -> EXIT -> ENTRY -> EXIT...
            TicketValidation mostRecentValidation = validations.get(0);
            ValidationType expectedValidationType = mostRecentValidation.getValidationType() == ValidationType.ENTRY 
                ? ValidationType.EXIT 
                : ValidationType.ENTRY;

            if (validationType != expectedValidationType) {
                throw new IllegalStateException(
                    String.format("Expected validation type: %s, but got: %s. Timed tickets must follow ENTRY-EXIT pattern.", 
                        expectedValidationType, validationType));
            }

            // If expected is EXIT, ensure stationId is different from ENTRY's stationId
            if (validationType == ValidationType.EXIT) {
                // The most recent validation is ENTRY
                String entryStationId = mostRecentValidation.getStationId();
                if (stationId.equals(entryStationId)) {
                    throw new IllegalStateException("EXIT station must be different from ENTRY station for timed ticket");
                }
            }
        }

        log.info("Validated timed ticket: {} with type: {} at station {}", ticket.getId(), validationType, stationId);
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