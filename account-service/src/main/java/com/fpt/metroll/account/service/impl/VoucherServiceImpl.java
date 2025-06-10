package com.fpt.metroll.account.service.impl;

import com.fpt.metroll.account.document.Voucher;
import com.fpt.metroll.account.domain.dto.VoucherCreateRequest;
import com.fpt.metroll.account.domain.dto.VoucherUpdateRequest;
import com.fpt.metroll.account.domain.mapper.VoucherMapper;
import com.fpt.metroll.account.repository.VoucherRepository;
import com.fpt.metroll.account.service.VoucherService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.voucher.VoucherDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.enums.VoucherStatus;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.shared.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VoucherServiceImpl implements VoucherService {

    private final MongoHelper mongoHelper;
    private final VoucherMapper voucherMapper;
    private final VoucherRepository voucherRepository;

    public VoucherServiceImpl(MongoHelper mongoHelper,
                            VoucherMapper voucherMapper,
                            VoucherRepository voucherRepository) {
        this.mongoHelper = mongoHelper;
        this.voucherMapper = voucherMapper;
        this.voucherRepository = voucherRepository;
    }

    private void validateVoucherAmounts(BigDecimal discountAmount, BigDecimal minTransactionAmount) {
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount amount must be positive");
        }
        if (minTransactionAmount != null && minTransactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Minimum transaction amount must be positive");
        }
    }

    private void validateValidityDates(Instant validFrom, Instant validUntil) {
        if (validFrom != null && validUntil != null) {
            Instant now = Instant.now();
            if (validFrom.isBefore(now)) {
                throw new IllegalArgumentException("Valid from date must be in the future");
            }
            if (validUntil.isBefore(validFrom.plus(Duration.ofHours(1)))) {
                throw new IllegalArgumentException("Valid until date must be at least 1 hour after valid from date");
            }
        } else if (validFrom != null || validUntil != null) {
            throw new IllegalArgumentException("Both valid from and valid until dates must be provided together");
        }
    }

    @Override
    public PageDto<VoucherDto> findAll(String userId, PageableDto pageable) {
        // For CUSTOMER role, only show their own vouchers
        if (SecurityUtil.hasRole(AccountRole.CUSTOMER)) {
            userId = SecurityUtil.requireUserId();
        }

        final String finalUserId = userId;

        var res = mongoHelper.find(query -> {
            if (finalUserId != null && !finalUserId.isBlank()) {
                query.addCriteria(Criteria.where("ownerId").is(finalUserId));
            }

            return query;
        }, pageable, Voucher.class).map(voucherMapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    public Optional<VoucherDto> findById(String id) {
        return voucherRepository.findById(id).map(e -> {
            if (SecurityUtil.hasRole(AccountRole.CUSTOMER) &&
                    !Objects.equals(e.getOwnerId(), SecurityUtil.requireUserId())) {
                throw new NoPermissionException();
            }
            return e;
        }).map(voucherMapper::toDto);
    }

    @Override
    public VoucherDto requireById(String id) {
        return voucherRepository.findById(id)
                .map(e -> {
                    if (SecurityUtil.hasRole(AccountRole.CUSTOMER) &&
                            !Objects.equals(e.getOwnerId(), SecurityUtil.requireUserId())) {
                        throw new NoPermissionException();
                    }
                    return e;
                })
                .map(voucherMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));
    }

    @Override
    public List<VoucherDto> create(VoucherCreateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.STAFF))
            throw new NoPermissionException();

        validateVoucherAmounts(request.getDiscountAmount(), request.getMinTransactionAmount());
        validateValidityDates(request.getValidFrom(), request.getValidUntil());

        List<Voucher> vouchers = request.getOwnerIds().stream()
                .map(ownerId -> {
                    String code;
                    do {
                        code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    } while (voucherRepository.existsByCode(code));

                    return Voucher.builder()
                            .ownerId(ownerId)
                            .code(code)
                            .discountAmount(request.getDiscountAmount())
                            .minTransactionAmount(request.getMinTransactionAmount())
                            .validFrom(request.getValidFrom())
                            .validUntil(request.getValidUntil())
                            .status(VoucherStatus.VALID)
                            .build();
                })
                .collect(Collectors.toList());

        vouchers = voucherRepository.saveAll(vouchers);
        return vouchers.stream().map(voucherMapper::toDto).toList();
    }

    @Override
    public VoucherDto update(String id, VoucherUpdateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.STAFF))
            throw new NoPermissionException();

        validateVoucherAmounts(request.getDiscountAmount(), request.getMinTransactionAmount());
        validateValidityDates(request.getValidFrom(), request.getValidUntil());

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));

        if (voucher.getStatus() != VoucherStatus.VALID)
            throw new IllegalStateException("Can only update VALID vouchers");

        if (request.getDiscountAmount() != null) {
            voucher.setDiscountAmount(request.getDiscountAmount());
        }
        if (request.getMinTransactionAmount() != null) {
            voucher.setMinTransactionAmount(request.getMinTransactionAmount());
        }
        if (request.getValidFrom() != null) {
            voucher.setValidFrom(request.getValidFrom());
        }
        if (request.getValidUntil() != null) {
            voucher.setValidUntil(request.getValidUntil());
        }

        voucher = voucherRepository.save(voucher);
        return voucherMapper.toDto(voucher);
    }

    @Override
    public void revoke(String id) {
        if (!SecurityUtil.hasRole(AccountRole.STAFF))
            throw new NoPermissionException();

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));

        if (voucher.getStatus() != VoucherStatus.VALID)
            throw new IllegalStateException("Can only revoke VALID vouchers");

        voucher.setStatus(VoucherStatus.REVOKED);
        voucherRepository.save(voucher);
    }
} 