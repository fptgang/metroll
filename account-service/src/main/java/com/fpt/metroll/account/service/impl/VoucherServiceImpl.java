package com.fpt.metroll.account.service.impl;

import com.fpt.metroll.account.document.Account;
import com.fpt.metroll.account.document.Voucher;
import com.fpt.metroll.account.domain.dto.VoucherCreateRequest;
import com.fpt.metroll.account.domain.dto.VoucherUpdateRequest;
import com.fpt.metroll.account.domain.mapper.VoucherMapper;
import com.fpt.metroll.account.repository.AccountRepository;
import com.fpt.metroll.account.repository.VoucherRepository;
import com.fpt.metroll.account.service.VoucherService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.email.TicketCompensationEmailContext;
import com.fpt.metroll.shared.domain.dto.email.VoucherEmailContext;
import com.fpt.metroll.shared.domain.dto.voucher.VoucherCompensationRequest;
import com.fpt.metroll.shared.domain.dto.voucher.VoucherDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.enums.EmailType;
import com.fpt.metroll.shared.domain.enums.VoucherStatus;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.service.EmailService;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VoucherServiceImpl implements VoucherService {

    private final MongoHelper mongoHelper;
    private final VoucherMapper voucherMapper;
    private final VoucherRepository voucherRepository;
    private final EmailService emailService;
    private final AccountRepository accountRepository;

    public VoucherServiceImpl(MongoHelper mongoHelper,
                              VoucherMapper voucherMapper,
                              VoucherRepository voucherRepository, EmailService emailService, AccountRepository accountRepository) {
        this.mongoHelper = mongoHelper;
        this.voucherMapper = voucherMapper;
        this.voucherRepository = voucherRepository;
        this.emailService = emailService;
        this.accountRepository = accountRepository;
    }

    private void validateVoucherAmounts(BigDecimal discountAmount, BigDecimal minTransactionAmount) {
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount amount must be positive");
        }
        if (minTransactionAmount != null && minTransactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Minimum transaction amount must be positive");
        }
        if (minTransactionAmount != null && discountAmount != null && minTransactionAmount.compareTo(discountAmount) <= 0) {
            throw new IllegalArgumentException("Minimum transaction amount must be greater than discount amount");
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
                query.addCriteria(Criteria.where("userId").is(finalUserId));
            }

            return query;
        }, pageable, Voucher.class).map(voucherMapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    public Optional<VoucherDto> findById(String id) {
        return voucherRepository.findById(id).map(e -> {
            if (SecurityUtil.hasRole(AccountRole.CUSTOMER) &&
                    e.getUserId() != null &&
                    !e.getUserId().equals(SecurityUtil.requireUserId())) {
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
                            e.getUserId() != null &&
                            !e.getUserId().equals(SecurityUtil.requireUserId())) {
                        throw new NoPermissionException();
                    }
                    return e;
                })
                .map(voucherMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));
    }

    @Override
    public VoucherDto requireByCode(String code) {
        code = code.toUpperCase();
        return voucherRepository.findByCode(code)
                .map(e -> {
                    if (SecurityUtil.hasRole(AccountRole.CUSTOMER) &&
                            e.getUserId() != null &&
                            !e.getUserId().equals(SecurityUtil.requireUserId())) {
                        throw new NoPermissionException();
                    }
                    return e;
                })
                .map(voucherMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));
    }

    @Override
    public List<VoucherDto> create(VoucherCreateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.STAFF, AccountRole.ADMIN))
            throw new NoPermissionException();

        validateVoucherAmounts(request.getDiscountAmount(), request.getMinTransactionAmount());
        validateValidityDates(request.getValidFrom(), request.getValidUntil());

        String issuerId = SecurityUtil.requireUserId();

        List<Voucher> vouchers = request.getRecipients().stream()
                .map(recipientId -> {
                    String code;

                    do {
                        code = generateRandomCode(6);
                    } while (voucherRepository.existsByCode(code));

                    Account account = accountRepository.findById(recipientId)
                            .orElseThrow(() -> new IllegalArgumentException("Account not found"));

                    emailService.sendVoucherEmail(account.getEmail(), account.getFullName(),
                            EmailType.VOUCHER_CLAIMED, VoucherEmailContext.builder()
                                    .voucherCode(code)
                                    .discountAmount(request.getDiscountAmount())
                                    .minTransactionAmount(request.getMinTransactionAmount())
                                    .validFrom(request.getValidFrom())
                                    .validUntil(request.getValidUntil())
                                    .status("AVAILABLE")
                                    .actionDate(Instant.now())
                                    .actionPerformedBy(SecurityUtil.requireUserRole().name())
                                    .actionReason("Issued to "+ SecurityUtil.requireUserRole().name())
                                    .build()
                    );

                    return Voucher.builder()
                            .issuerId(issuerId)
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
        if (!SecurityUtil.hasRole(AccountRole.STAFF, AccountRole.ADMIN))
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
        if (!SecurityUtil.hasRole(AccountRole.STAFF, AccountRole.ADMIN))
            throw new NoPermissionException();

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));

        if (voucher.getStatus() != VoucherStatus.VALID)
            throw new IllegalStateException("Can only revoke VALID vouchers");

        voucher.setStatus(VoucherStatus.REVOKED);
        voucherRepository.save(voucher);
    }

    @Override
    public void use(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN)) // internal use
            throw new NoPermissionException();

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));

        if (voucher.getStatus() != VoucherStatus.PRESERVED)
            throw new IllegalStateException("Can only use PRESERVED vouchers");

        Preconditions.checkNotNull(voucher.getUserId(), "No user id");
        Preconditions.checkState(voucher.getUserId().equals(SecurityUtil.requireUserId()),
                "Illegal use of preserved user");

        voucher.setStatus(VoucherStatus.USED);
        voucherRepository.save(voucher);
    }

    @Override
    public void preserve(String id, String userId) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN)) // internal use
            throw new NoPermissionException();

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));

        Preconditions.checkState(voucher.getStatus() == VoucherStatus.VALID,
                "Can only preserve VALID vouchers");
        Preconditions.checkState(voucher.getUserId() == null,
                "Voucher user already exists");

        voucher.setStatus(VoucherStatus.PRESERVED);
        voucher.setUserId(SecurityUtil.requireUserId());
        voucherRepository.save(voucher);
    }

    @Override
    public void unpreserve(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN)) // internal use
            throw new NoPermissionException();

        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));

        Preconditions.checkState(voucher.getStatus() == VoucherStatus.PRESERVED,
                "Can only un-preserve PRESERVED vouchers");

        voucher.setStatus(VoucherStatus.VALID);
        voucher.setUserId(null);
        voucherRepository.save(voucher);
    }

    //@Transactional maybe
    //@Async send email part if too slow
    @Override
    public Boolean createCompensationVoucher(List<VoucherCompensationRequest> request) {
        List<Voucher> vouchers = request.stream()
                .map(req -> {
                    String code;
                    do {
                        code = generateRandomCode(6);
                    } while (voucherRepository.existsByCode(code));

                    Account account = accountRepository.findById(req.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("Account not found for userId: " + req.getUserId()));

                    TicketCompensationEmailContext context = TicketCompensationEmailContext.builder()
                            .cancelledTicketId(req.getCompensationId())
                            .fromStationName(req.getFromStationName())
                            .toStationName(req.getToStationName())
                            .voucherCode(code)
                            .discountAmount(req.getDiscountAmount())
                            .minTransactionAmount(req.getMinTransactionAmount())
                            .validFrom(req.getValidFrom())
                            .validUntil(req.getValidUntil())
                            .build();

                    emailService.sendTicketCompensationEmail(
                            account.getEmail(),
                            account.getFullName(),
                            context
                    );

                    return Voucher.builder()
                            .code(code)
                            .discountAmount(req.getDiscountAmount())
                            .minTransactionAmount(req.getMinTransactionAmount())
                            .validFrom(req.getValidFrom())
                            .validUntil(req.getValidUntil())
                            .status(VoucherStatus.VALID)
                            .userId(req.getUserId())
                            .build();
                })
                .collect(Collectors.toList());

        voucherRepository.saveAll(vouchers);


        return true;
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }
}