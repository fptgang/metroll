package com.fpt.metroll.account.service.impl;

import com.fpt.metroll.account.document.Account;
import com.fpt.metroll.account.document.AccountDiscountPackage;
import com.fpt.metroll.account.document.DiscountPackage;
import com.fpt.metroll.account.domain.dto.AccountDiscountAssignRequest;
import com.fpt.metroll.account.domain.mapper.AccountDiscountPackageMapper;
import com.fpt.metroll.account.repository.AccountDiscountPackageRepository;
import com.fpt.metroll.account.repository.AccountRepository;
import com.fpt.metroll.account.repository.DiscountPackageRepository;
import com.fpt.metroll.account.service.AccountDiscountPackageService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.discount.AccountDiscountPackageDto;
import com.fpt.metroll.shared.domain.enums.AccountDiscountStatus;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.List;

@Slf4j
@Service
public class AccountDiscountPackageServiceImpl implements AccountDiscountPackageService {

    private final MongoHelper mongoHelper;
    private final AccountDiscountPackageMapper accountDiscountPackageMapper;
    private final AccountDiscountPackageRepository accountDiscountPackageRepository;
    private final AccountRepository accountRepository;
    private final DiscountPackageRepository discountPackageRepository;

    public AccountDiscountPackageServiceImpl(MongoHelper mongoHelper,
            AccountDiscountPackageMapper accountDiscountPackageMapper,
            AccountDiscountPackageRepository accountDiscountPackageRepository,
            AccountRepository accountRepository,
            DiscountPackageRepository discountPackageRepository) {
        this.mongoHelper = mongoHelper;
        this.accountDiscountPackageMapper = accountDiscountPackageMapper;
        this.accountDiscountPackageRepository = accountDiscountPackageRepository;
        this.accountRepository = accountRepository;
        this.discountPackageRepository = discountPackageRepository;
    }

    @Override
    public PageDto<AccountDiscountPackageDto> findAll(String accountId, String packageId, PageableDto pageable) {
        // For CUSTOMER role, only show their own assigned discount packages
        if (SecurityUtil.hasRole(AccountRole.CUSTOMER)) {
            accountId = SecurityUtil.requireUserId();
        }

        final String finalAccountId = accountId;

        var res = mongoHelper.find(query -> {
            if (finalAccountId != null && !finalAccountId.isBlank()) {
                query.addCriteria(Criteria.where("accountId").is(finalAccountId));
            }
            if (packageId != null && !packageId.isBlank()) {
                query.addCriteria(Criteria.where("discountPackageId").is(packageId));
            }

            return query;
        }, pageable, AccountDiscountPackage.class).map(accountDiscountPackageMapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    public Optional<AccountDiscountPackageDto> findById(String id) {
        Preconditions.checkNotNull(id, "ID cannot be null");
        return accountDiscountPackageRepository.findById(id)
                .map(e -> {
                    if (SecurityUtil.hasRole(AccountRole.CUSTOMER) &&
                            !Objects.equals(e.getAccountId(), SecurityUtil.requireUserId())) {
                        throw new NoPermissionException();
                    }
                    return e;
                })
                .map(accountDiscountPackageMapper::toDto);
    }

    @Override
    public AccountDiscountPackageDto requireById(String id) {
        return findById(id)
                .map(e -> {
                    if (SecurityUtil.hasRole(AccountRole.CUSTOMER) &&
                            !Objects.equals(e.getAccountId(), SecurityUtil.requireUserId())) {
                        throw new NoPermissionException();
                    }
                    return e;
                })
                .orElseThrow(() -> new IllegalArgumentException("Account discount package not found"));
    }

    @Override
    public AccountDiscountPackageDto assign(AccountDiscountAssignRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.STAFF))
            throw new NoPermissionException();

        Preconditions.checkNotNull(request, "Request cannot be null");
        Preconditions.checkArgument(request.getAccountId() != null && !request.getAccountId().isBlank(),
                "Account ID cannot be blank");
        Preconditions.checkArgument(request.getDiscountPackageId() != null && !request.getDiscountPackageId().isBlank(),
                "Discount package ID cannot be blank");

        // Validate discount package is ACTIVE
        DiscountPackage discountPackage = discountPackageRepository.findById(request.getDiscountPackageId())
                .orElseThrow(() -> new IllegalArgumentException("Discount package not found"));

        if (discountPackage.getStatus() != DiscountPackageStatus.ACTIVE) {
            throw new IllegalStateException("Discount package must be ACTIVE");
        }

        // Validate target account is CUSTOMER
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getRole() != AccountRole.CUSTOMER) {
            throw new IllegalStateException("Can only assign discount package to CUSTOMER accounts");
        }

        // Check if account has no ongoing discount package
        Instant now = Instant.now();
        Optional<AccountDiscountPackage> existingPackage = accountDiscountPackageRepository
                .findByAccountIdAndStatusAndValidUntilAfter(
                        request.getAccountId(), AccountDiscountStatus.ACTIVATED, now);

        if (existingPackage.isPresent()) {
            throw new IllegalStateException("Account already has an ongoing discount package");
        }

        // Create new AccountDiscountPackage
        Instant activateDate = now;
        Instant validUntil = activateDate.plus(discountPackage.getDuration(), ChronoUnit.DAYS);

        AccountDiscountPackage accountDiscountPackage = AccountDiscountPackage.builder()
                .accountId(request.getAccountId())
                .discountPackageId(request.getDiscountPackageId())
                .activateDate(activateDate)
                .validUntil(validUntil)
                .status(AccountDiscountStatus.ACTIVATED)
                .build();

        accountDiscountPackage = accountDiscountPackageRepository.save(accountDiscountPackage);
        return accountDiscountPackageMapper.toDto(accountDiscountPackage);
    }

    @Override
    public void unassign(String id) {
        if (!SecurityUtil.hasRole(AccountRole.STAFF))
            throw new NoPermissionException();

        Preconditions.checkNotNull(id, "ID cannot be null");

        AccountDiscountPackage accountDiscountPackage = accountDiscountPackageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account discount package not found"));

        // Validate target account is CUSTOMER
        Account account = accountRepository.findById(accountDiscountPackage.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getRole() != AccountRole.CUSTOMER) {
            throw new IllegalStateException("Can only unassign discount package from CUSTOMER accounts");
        }

        // Check if the discount package is ongoing
        if (accountDiscountPackage.getStatus() != AccountDiscountStatus.ACTIVATED ||
                accountDiscountPackage.getValidUntil().isBefore(Instant.now())) {
            throw new IllegalStateException("Can only unassign ongoing discount packages");
        }

        accountDiscountPackage.setStatus(AccountDiscountStatus.CANCELLED);
        accountDiscountPackageRepository.save(accountDiscountPackage);
    }

    @Override
    public AccountDiscountPackageDto findMyActivatedDiscounts() {
        String currentUserId = SecurityUtil.requireUserId();
        Instant now = Instant.now();

        Optional<AccountDiscountPackage> activatedDiscount = accountDiscountPackageRepository
                .findByAccountIdAndStatusAndValidUntilAfter(currentUserId, AccountDiscountStatus.ACTIVATED, now);

        return accountDiscountPackageMapper.toDto(activatedDiscount.orElse(null));
    }
}