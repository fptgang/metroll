package com.fpt.metroll.ticket.service.impl;

import com.fpt.metroll.ticket.document.TimedTicketPlan;
import com.fpt.metroll.ticket.domain.dto.TimedTicketPlanCreateRequest;
import com.fpt.metroll.ticket.domain.dto.TimedTicketPlanUpdateRequest;
import com.fpt.metroll.ticket.domain.mapper.TimedTicketPlanMapper;
import com.fpt.metroll.ticket.repository.TimedTicketPlanRepository;
import com.fpt.metroll.ticket.service.TimedTicketPlanService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.ticket.TimedTicketPlanDto;
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

import java.util.Optional;

@Slf4j
@Service
@CacheConfig(cacheNames = "timed-ticket-plans")
public class TimedTicketPlanServiceImpl implements TimedTicketPlanService {

    private final MongoHelper mongoHelper;
    private final TimedTicketPlanMapper mapper;
    private final TimedTicketPlanRepository repository;

    public TimedTicketPlanServiceImpl(MongoHelper mongoHelper,
            TimedTicketPlanMapper mapper,
            TimedTicketPlanRepository repository) {
        this.mongoHelper = mongoHelper;
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    @Cacheable(key = "'findAll:' + (#search != null ? #search : 'null') + ':' + #pageable.page + ':' + #pageable.size + ':' + (#pageable.sort != null ? #pageable.sort : 'null')")
    public PageDto<TimedTicketPlanDto> findAll(String search, PageableDto pageable) {
        // Anyone can view ticket plans
        var res = mongoHelper.find(query -> {
            if (search != null && !search.isBlank()) {
                Criteria criteria = new Criteria().orOperator(
                        Criteria.where("name").regex(search, "i"));
                query.addCriteria(criteria);
            }
            return query;
        }, pageable, TimedTicketPlan.class).map(mapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    @Cacheable(key = "'findById:' + #id")
    public Optional<TimedTicketPlanDto> findById(String id) {
        Preconditions.checkNotNull(id, "ID cannot be null");
        return repository.findById(id).map(mapper::toDto);
    }

    @Override
    @Cacheable(key = "'requireById:' + #id")
    public TimedTicketPlanDto requireById(String id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Timed ticket plan not found"));
    }

    @Override
    @CacheEvict(allEntries = true)
    public TimedTicketPlanDto create(TimedTicketPlanCreateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(request, "Request cannot be null");
        Preconditions.checkArgument(request.getName() != null && !request.getName().isBlank(),
                "Name cannot be null or blank");
        Preconditions.checkArgument(request.getValidDuration() != null && request.getValidDuration() > 0,
                "Valid duration must be positive");
        Preconditions.checkArgument(request.getBasePrice() != null && request.getBasePrice() >= 0,
                "Base price must be non-negative");

        if (repository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Timed ticket plan with this name already exists");
        }

        TimedTicketPlan document = mapper.toDocument(request);
        document = repository.save(document);
        log.info("Created timed ticket plan: {}", document.getId());
        return mapper.toDto(document);
    }

    @Override
    @CacheEvict(allEntries = true)
    public TimedTicketPlanDto update(String id, TimedTicketPlanUpdateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");
        Preconditions.checkNotNull(request, "Request cannot be null");

        TimedTicketPlan document = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Timed ticket plan not found"));

        // Check if name already exists for a different plan
        if (request.getName() != null && !request.getName().equals(document.getName()) &&
                repository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Timed ticket plan with this name already exists");
        }

        document = mapper.updateFromRequest(document, request);
        document = repository.save(document);
        log.info("Updated timed ticket plan: {}", document.getId());
        return mapper.toDto(document);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void delete(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");

        TimedTicketPlan document = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Timed ticket plan not found"));

        repository.delete(document);
        log.info("Deleted timed ticket plan: {}", id);
    }
}