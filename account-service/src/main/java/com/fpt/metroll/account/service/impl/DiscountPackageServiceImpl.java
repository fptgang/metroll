package com.fpt.metroll.account.service.impl;

import com.fpt.metroll.account.document.DiscountPackage;
import com.fpt.metroll.account.domain.dto.DiscountPackageCreateRequest;
import com.fpt.metroll.account.domain.dto.DiscountPackageUpdateRequest;
import com.fpt.metroll.account.domain.mapper.DiscountPackageMapper;
import com.fpt.metroll.account.repository.DiscountPackageRepository;
import com.fpt.metroll.account.service.DiscountPackageService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.discount.DiscountPackageDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.enums.DiscountPackageStatus;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class DiscountPackageServiceImpl implements DiscountPackageService {

    private final MongoHelper mongoHelper;
    private final DiscountPackageMapper discountPackageMapper;
    private final DiscountPackageRepository discountPackageRepository;

    public DiscountPackageServiceImpl(MongoHelper mongoHelper,
                                     DiscountPackageMapper discountPackageMapper,
                                     DiscountPackageRepository discountPackageRepository) {
        this.mongoHelper = mongoHelper;
        this.discountPackageMapper = discountPackageMapper;
        this.discountPackageRepository = discountPackageRepository;
    }

    @Override
    public PageDto<DiscountPackageDto> findAll(String search, PageableDto pageable) {
        var res = mongoHelper.find(query -> {
            if (search != null && !search.isBlank()) {
                Criteria criteria = new Criteria().orOperator(
                        Criteria.where("name").regex(search, "i"),
                        Criteria.where("description").regex(search, "i"));
                query.addCriteria(criteria);
            }
            return query;
        }, pageable, DiscountPackage.class).map(discountPackageMapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    public Optional<DiscountPackageDto> findById(String id) {
        Preconditions.checkNotNull(id, "ID cannot be null");
        return discountPackageRepository.findById(id).map(discountPackageMapper::toDto);
    }

    @Override
    public DiscountPackageDto requireById(String id) {
        Preconditions.checkNotNull(id, "ID cannot be null");
        return discountPackageRepository.findById(id)
                .map(discountPackageMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Discount package not found"));
    }

    @Override
    public DiscountPackageDto create(DiscountPackageCreateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(request, "Request cannot be null");
        Preconditions.checkArgument(request.getName() != null && !request.getName().isBlank(), 
                "Name cannot be blank");
        Preconditions.checkArgument(request.getDescription() != null && !request.getDescription().isBlank(), 
                "Description cannot be blank");
        Preconditions.checkArgument(request.getDiscountPercentage() != null, 
                "Discount percentage cannot be null");
        Preconditions.checkArgument(request.getDuration() != null, 
                "Duration cannot be null");

        DiscountPackage discountPackage = discountPackageMapper.toDocument(request);
        discountPackage.setStatus(DiscountPackageStatus.ACTIVE);
        discountPackage = discountPackageRepository.save(discountPackage);

        return discountPackageMapper.toDto(discountPackage);
    }

    @Override
    public DiscountPackageDto update(String id, DiscountPackageUpdateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");
        Preconditions.checkNotNull(request, "Request cannot be null");

        DiscountPackage discountPackage = discountPackageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount package not found"));

        if (discountPackage.getStatus() != DiscountPackageStatus.ACTIVE)
            throw new IllegalStateException("Can only update ACTIVE discount packages");

        discountPackage.setName(request.getName());
        discountPackage.setDescription(request.getDescription());
        discountPackage.setDiscountPercentage(request.getDiscountPercentage());
        discountPackage.setDuration(request.getDuration());

        discountPackage = discountPackageRepository.save(discountPackage);
        return discountPackageMapper.toDto(discountPackage);
    }

    @Override
    public void terminate(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");

        DiscountPackage discountPackage = discountPackageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount package not found"));

        if (discountPackage.getStatus() != DiscountPackageStatus.ACTIVE)
            throw new IllegalStateException("Can only terminate ACTIVE discount packages");

        discountPackage.setStatus(DiscountPackageStatus.TERMINATED);
        discountPackageRepository.save(discountPackage);
    }
} 