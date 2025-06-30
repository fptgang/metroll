package com.fpt.metroll.account.service.impl;

import com.fpt.metroll.account.document.Account;
import com.fpt.metroll.account.document.DiscountPackage;
import com.fpt.metroll.account.document.Voucher;
import com.fpt.metroll.account.domain.dto.AccountCreateRequest;
import com.fpt.metroll.account.domain.dto.AccountDashboardDto;
import com.fpt.metroll.account.domain.dto.AccountUpdateRequest;
import com.fpt.metroll.account.domain.dto.StationAssignRequest;
import com.fpt.metroll.account.domain.mapper.AccountMapper;
import com.fpt.metroll.account.repository.AccountRepository;
import com.fpt.metroll.account.repository.DiscountPackageRepository;
import com.fpt.metroll.account.repository.VoucherRepository;
import com.fpt.metroll.account.service.AccountService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.enums.DiscountPackageStatus;
import com.fpt.metroll.shared.domain.enums.VoucherStatus;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.shared.util.SecurityUtil;
import com.google.common.base.Preconditions;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Criteria;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.FirebaseAuthException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final MongoHelper mongoHelper;
    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;
    private final DiscountPackageRepository discountPackageRepository;
    private final VoucherRepository voucherRepository;

    public AccountServiceImpl(MongoHelper mongoHelper,
            AccountMapper accountMapper,
            AccountRepository accountRepository,
            DiscountPackageRepository discountPackageRepository,
            VoucherRepository voucherRepository) {
        this.mongoHelper = mongoHelper;
        this.accountMapper = accountMapper;
        this.accountRepository = accountRepository;
        this.discountPackageRepository = discountPackageRepository;
        this.voucherRepository = voucherRepository;
    }

    @Override
    public PageDto<AccountDto> findAll(String search, PageableDto pageable) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        var res = mongoHelper.find(query -> {
            if (search != null && !search.isBlank()) {
                Criteria criteria = new Criteria().orOperator(
                        Criteria.where("fullName").regex(search, "i"),
                        Criteria.where("email").regex(search, "i"),
                        Criteria.where("phoneNumber").regex(search, "i"));
                query.addCriteria(criteria);
            }

            // Staff can only see Customer
            if (SecurityUtil.hasRole(AccountRole.STAFF)) {
                query.addCriteria(Criteria.where("role")
                        .is(AccountRole.CUSTOMER.name()));
            }

            return query;
        }, pageable, Account.class).map(accountMapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    public PageDto<AccountDto> findStaff(String search, PageableDto pageable) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        var res = mongoHelper.find(query -> {
            // Filter only STAFF role accounts
            query.addCriteria(Criteria.where("role").is(AccountRole.STAFF.name()));

            if (search != null && !search.isBlank()) {
                Criteria criteria = new Criteria().orOperator(
                        Criteria.where("fullName").regex(search, "i"),
                        Criteria.where("email").regex(search, "i"),
                        Criteria.where("phoneNumber").regex(search, "i"));
                query.addCriteria(criteria);
            }

            return query;
        }, pageable, Account.class).map(accountMapper::toDto);
        return PageMapper.INSTANCE.toPageDTO(res);
    }

    @Override
    public Optional<AccountDto> findById(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF)
                && !Objects.equals(SecurityUtil.getUserId(), id))
            throw new NoPermissionException();

        return accountRepository.findById(id).map(accountMapper::toDto);
    }

    @Override
    public AccountDto requireById(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF)
                && !Objects.equals(SecurityUtil.getUserId(), id))
            throw new NoPermissionException();

        return accountRepository.findById(id)
                .map(accountMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    @Override
    public AccountDto create(AccountCreateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        try {
            var userRecord = FirebaseAuth.getInstance().createUser(new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setDisplayName(request.getFullName())
                    .setPhoneNumber(request.getPhoneNumber())
                    .setEmailVerified(false));

            Account account = accountMapper.toDocument(request);
            account.setId(userRecord.getUid());
            account.setActive(true);
            account.setCreatedAt(Instant.now());
            account = accountRepository.save(account);

            return accountMapper.toDto(account);
        } catch (FirebaseAuthException e) {
            throw new IllegalStateException("Failed to create Firebase user", e);
        }
    }

    @Override
    public AccountDto create(String id, AccountCreateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Account account = accountMapper.toDocument(request);
        account.setId(id);
        account.setActive(true);
        account.setCreatedAt(Instant.now());
        account = accountRepository.save(account);
        return accountMapper.toDto(account);
    }

    @Override
    public AccountDto update(String id, AccountUpdateRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF)
                && !Objects.equals(SecurityUtil.getUserId(), id))
            throw new NoPermissionException();

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Cannot update account with higher rank
        if (account.getRole().hasHigherRankThan(SecurityUtil.requireUserRole()))
            throw new NoPermissionException();

        account.setFullName(request.getFullName());
        account.setPhoneNumber(request.getPhoneNumber());

        // Only admin can update role
        if (SecurityUtil.hasRole(AccountRole.ADMIN)) {
            account.setRole(request.getRole());
        }

        account = accountRepository.save(account);
        return accountMapper.toDto(account);
    }

    @Override
    public void deactivate(String id) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN) ||
                Objects.equals(SecurityUtil.requireUserId(), id))
            throw new NoPermissionException();

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Cannot deactivate account with higher-or-same role
        if (account.getRole().hasHigherOrEqualRank(SecurityUtil.requireUserRole()))
            throw new NoPermissionException();

        account.setActive(false);
        accountRepository.save(account);

    }

    @Override
    public AccountDto assignStation(String accountId, StationAssignRequest request) {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN))
            throw new NoPermissionException();

        Preconditions.checkNotNull(accountId, "Account ID cannot be null");
        Preconditions.checkNotNull(request, "Request cannot be null");
        Preconditions.checkArgument(request.getStationCode() != null && !request.getStationCode().isBlank(),
                "Station ID cannot be blank");

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Only STAFF accounts can be assigned to stations
        if (account.getRole() != AccountRole.STAFF) {
            throw new IllegalStateException("Only STAFF accounts can be assigned to stations");
        }

        account.setAssignedStation(request.getStationCode());
        account = accountRepository.save(account);

        return accountMapper.toDto(account);
    }

    @Override
    public AccountDashboardDto getDashboard() {
        if (!SecurityUtil.hasRole(AccountRole.ADMIN, AccountRole.STAFF))
            throw new NoPermissionException();

        // Get all accounts
        var allAccounts = accountRepository.findAll();

        // Count accounts by role
        Map<String, Long> accountsByRole = new HashMap<>();
        for (AccountRole role : AccountRole.values()) {
            long count = allAccounts.stream()
                    .filter(account -> account.getRole() == role)
                    .count();
            accountsByRole.put(role.name(), count);
        }

        // Count active/inactive accounts
        long activeAccounts = allAccounts.stream()
                .filter(account -> Boolean.TRUE.equals(account.getActive()))
                .count();
        long inactiveAccounts = allAccounts.stream()
                .filter(account -> Boolean.FALSE.equals(account.getActive()))
                .count();

        // Count staff with/without assigned stations
        long staffWithAssignedStation = allAccounts.stream()
                .filter(account -> account.getRole() == AccountRole.STAFF)
                .filter(account -> account.getAssignedStation() != null && !account.getAssignedStation().isBlank())
                .count();
        long staffWithoutAssignedStation = allAccounts.stream()
                .filter(account -> account.getRole() == AccountRole.STAFF)
                .filter(account -> account.getAssignedStation() == null || account.getAssignedStation().isBlank())
                .count();

        // Get discount package stats
        var allDiscountPackages = discountPackageRepository.findAll();
        long totalDiscountPackages = allDiscountPackages.size();
        long activeDiscountPackages = allDiscountPackages.stream()
                .filter(pkg -> pkg.getStatus() == DiscountPackageStatus.ACTIVE)
                .count();

        // Get voucher stats
        var allVouchers = voucherRepository.findAll();
        long totalVouchers = allVouchers.size();
        BigDecimal totalVoucherValue = allVouchers.stream()
                .filter(voucher -> voucher.getStatus() == VoucherStatus.VALID)
                .map(Voucher::getDiscountAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AccountDashboardDto.builder()
                .totalAccounts((long) allAccounts.size())
                .accountsByRole(accountsByRole)
                .activeAccounts(activeAccounts)
                .inactiveAccounts(inactiveAccounts)
                .staffWithAssignedStation(staffWithAssignedStation)
                .staffWithoutAssignedStation(staffWithoutAssignedStation)
                .totalDiscountPackages(totalDiscountPackages)
                .activeDiscountPackages(activeDiscountPackages)
                .totalVouchers(totalVouchers)
                .totalVoucherValue(totalVoucherValue)
                .lastUpdated(Instant.now())
                .build();
    }

}
