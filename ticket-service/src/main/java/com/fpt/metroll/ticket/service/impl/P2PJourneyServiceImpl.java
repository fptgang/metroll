package com.fpt.metroll.ticket.service.impl;

import com.fpt.metroll.shared.domain.client.SubwayClient;
import com.fpt.metroll.shared.domain.dto.subway.StationDto;
import com.fpt.metroll.shared.domain.dto.subway.StationQueryParam;
import com.fpt.metroll.ticket.document.P2PJourney;
import com.fpt.metroll.ticket.domain.dto.P2PJourneyCreateRequest;
import com.fpt.metroll.ticket.domain.dto.P2PJourneyUpdateRequest;
import com.fpt.metroll.ticket.domain.mapper.P2PJourneyMapper;
import com.fpt.metroll.ticket.repository.P2PJourneyRepository;
import com.fpt.metroll.ticket.service.P2PJourneyService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.P2PJourneyDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@CacheConfig(cacheNames = "p2p-journeys")
public class P2PJourneyServiceImpl implements P2PJourneyService {

    private final MongoHelper mongoHelper;
    private final P2PJourneyMapper mapper;
    private final P2PJourneyRepository repository;
    private final SubwayClient subwayClient;

    public P2PJourneyServiceImpl(MongoHelper mongoHelper,
                                 P2PJourneyMapper mapper,
                                 P2PJourneyRepository repository, SubwayClient subwayClient) {
        this.mongoHelper = mongoHelper;
        this.mapper = mapper;
        this.repository = repository;
        this.subwayClient = subwayClient;
    }

    @Override
//    @Cacheable(key = "'findAll:' + (#search != null ? #search : 'null') + ':' + #pageable.page + ':' + #pageable.size + ':' + (#pageable.sort != null ? #pageable.sort : 'null') + ':' ")
    public PageDto<P2PJourneyDto> findAll(String search, PageableDto pageable) {
        // Anyone can view P2P journeys

        List<StationDto> unavailableStations = subwayClient.listUnavailableStations();
        List<String> unavailableStationCodes = unavailableStations.stream().map(StationDto::getCode).toList();

        log.info("Unavailable stations: {}", unavailableStationCodes.size());
        unavailableStationCodes.forEach(code -> log.info("Unavailable station: {}", code));

        var res = mongoHelper.find(query -> {
            // Filter only active records
            if (!SecurityUtil.hasRole(AccountRole.ADMIN)) {
                query.addCriteria(Criteria.where("isActive").is(true));
                query.addCriteria(Criteria.where("startStationId").nin(unavailableStationCodes));
                query.addCriteria(Criteria.where("endStationId").nin(unavailableStationCodes));
            }

            if (search != null && !search.isBlank()) {
                Criteria criteria = new Criteria().orOperator(
                        Criteria.where("startStationId").regex(search, "i"),
                        Criteria.where("endStationId").regex(search, "i"));
                query.addCriteria(criteria);
            }
            return query;
        }, pageable, P2PJourney.class).map(mapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    @Cacheable(key = "'findById:' + #id")
    public Optional<P2PJourneyDto> findById(String id) {
        Preconditions.checkNotNull(id, "ID cannot be null");
        if (!SecurityUtil.hasRole(AccountRole.ADMIN)) {
            // Only active journeys for non-admin users
            return repository.findByIdAndIsActiveTrue(id).map(mapper::toDto);
        }
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Cacheable(key = "'requireById:' + #id")
    public P2PJourneyDto requireById(String id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("P2P journey not found"));
    }

    @Override
//    @Cacheable(key = "'findByStations:' + (#startStationId != null ? #startStationId : 'null') + ':' + (#endStationId != null ? #endStationId : 'null') + ':' + #pageable.page + ':' + #pageable.size + ':' + (#pageable.sort != null ? #pageable.sort : 'null')")
    public PageDto<P2PJourneyDto> findByStations(PageableDto pageable, String startStationId, String endStationId) {
        Criteria criteria = new Criteria();
        List<StationDto> unavailableStations = subwayClient.listUnavailableStations();
        List<String> unavailableStationCodes = unavailableStations.stream().map(StationDto::getCode).toList();
        // Always filter by active records
        if (!SecurityUtil.hasRole(AccountRole.ADMIN)) {
            criteria.and("isActive").is(true);
            criteria.and("startStationId").nin(unavailableStationCodes);
            criteria.and("endStationId").nin(unavailableStationCodes);
        }

        if ((startStationId != null && endStationId != null)
                && (!startStationId.isEmpty() && !endStationId.isEmpty())) {
            criteria.andOperator(
                    Criteria.where("startStationId").is(startStationId),
                    Criteria.where("endStationId").is(endStationId));
        } else if (startStationId != null && !startStationId.isEmpty()) {
            criteria.andOperator(Criteria.where("startStationId").is(startStationId));
        } else if (endStationId != null && !endStationId.isEmpty()) {
            criteria.andOperator(Criteria.where("endStationId").is(endStationId));
        } else {
            return findAll(null, pageable);
        }

        var res = mongoHelper.find(query -> {
            query.addCriteria(criteria);
            return query;
        }, pageable, P2PJourney.class).map(mapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    @CacheEvict(allEntries = true)
    public P2PJourneyDto create(P2PJourneyCreateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(request, "Request cannot be null");
        Preconditions.checkArgument(request.getStartStationId() != null && !request.getStartStationId().isBlank(),
                "Start station ID cannot be null or blank");
        Preconditions.checkArgument(request.getEndStationId() != null && !request.getEndStationId().isBlank(),
                "End station ID cannot be null or blank");
        Preconditions.checkArgument(!request.getStartStationId().equals(request.getEndStationId()),
                "Start and end stations must be different");
        Preconditions.checkArgument(request.getBasePrice() != null && request.getBasePrice() >= 0,
                "Base price must be non-negative");
        Preconditions.checkArgument(request.getDistance() != null && request.getDistance() > 0,
                "Distance must be positive");
        Preconditions.checkArgument(request.getTravelTime() != null && request.getTravelTime() > 0,
                "Travel time must be positive");

        // Check if journey already exists
        if (repository.findByStartStationIdAndEndStationId(request.getStartStationId(), request.getEndStationId())
                .isPresent()) {
            throw new IllegalArgumentException("P2P journey for these stations already exists");
        }

        validateStations(request.getStartStationId(), request.getEndStationId());

        P2PJourney document = mapper.toDocument(request);
        document = repository.save(document);
        log.info("Created P2P journey: {}", document.getId());
        return mapper.toDto(document);
    }

    @Override
    @CacheEvict(allEntries = true)
    public P2PJourneyDto update(String id, P2PJourneyUpdateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");
        Preconditions.checkNotNull(request, "Request cannot be null");

        P2PJourney document = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("P2P journey not found"));

        // Validate new station combination if stations are being updated
        if (request.getStartStationId() != null || request.getEndStationId() != null) {
            String newStartStation = request.getStartStationId() != null ? request.getStartStationId()
                    : document.getStartStationId();
            String newEndStation = request.getEndStationId() != null ? request.getEndStationId()
                    : document.getEndStationId();

            if (newStartStation.equals(newEndStation)) {
                throw new IllegalArgumentException("Start and end stations must be different");
            }

            // Check if this combination already exists for a different journey
            repository.findByStartStationIdAndEndStationId(newStartStation, newEndStation)
                    .ifPresent(existingJourney -> {
                        if (!existingJourney.getId().equals(id)) {
                            throw new IllegalArgumentException("P2P journey for these stations already exists");
                        }
                    });
        }
        validateStations(request.getStartStationId(), request.getEndStationId());
        document = mapper.updateFromRequest(document, request);
        document = repository.save(document);
        log.info("Updated P2P journey: {}", document.getId());
        return mapper.toDto(document);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void delete(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");

        P2PJourney document = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("P2P journey not found"));

        // Soft delete: set isActive to false instead of hard delete
        document.setIsActive(false);
        repository.save(document);
        log.info("Soft deleted P2P journey: {}", id);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deactivateP2PJourneyByStation(String stationId) {
        repository.findByStartStationIdOrEndStationId(stationId, stationId).forEach(document -> {
            // Soft delete: set isActive to false instead of hard delete
            document.setIsActive(false);
            repository.save(document);
            log.info("Soft deleted P2P journey: {}", document.getId());
        });

    }

    @Override
    @CacheEvict(allEntries = true)
    public P2PJourneyDto activate(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");

        P2PJourney document = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("P2P journey not found"));

        if (document.getIsActive()) {
            throw new IllegalArgumentException("P2P journey is already active");
        }

        // Check if there's already an active journey with the same station combination
        if (repository.findByStartStationIdAndEndStationIdAndIsActiveTrue(
                document.getStartStationId(), document.getEndStationId()).isPresent()) {
            throw new IllegalArgumentException("An active P2P journey for these stations already exists");
        }
        validateStations(document.getStartStationId(), document.getEndStationId());
        // Activate: set isActive to true
        document.setIsActive(true);
        document = repository.save(document);
        log.info("Activated P2P journey: {}", id);
        return mapper.toDto(document);
    }

    public void validateStations(String startStationId, String endStationId) {
        StationDto startStation = subwayClient.getStationByCode(startStationId);
        StationDto endStation = subwayClient.getStationByCode(endStationId);

        if (startStation == null || endStation == null) {
            throw new IllegalArgumentException("Start or end station not found");
        } else {
            if (startStation.getId().equals(endStation.getId())) {
                throw new IllegalArgumentException("Start and end stations must be different");
            } else if (startStation.getStatus().equals("CLOSED") || endStation.getStatus().equals("CLOSED")) {
                throw new IllegalArgumentException("Start and end stations must be open :\n" + startStationId + " - " + startStation.getStatus() + ", \n " + endStationId + " - " + endStation.getStatus());
            }
        }
    }
}