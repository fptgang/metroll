package com.fpt.metroll.account.service.impl;

import com.fpt.metroll.account.document.Account;
import com.fpt.metroll.account.domain.dto.AccountCreateRequest;
import com.fpt.metroll.account.domain.dto.AccountUpdateRequest;
import com.fpt.metroll.account.domain.mapper.AccountMapper;
import com.fpt.metroll.account.repository.AccountRepository;
import com.fpt.metroll.account.service.AccountService;
import com.fpt.metroll.shared.domain.dto.PageDto;
import com.fpt.metroll.shared.domain.dto.PageableDto;
import com.fpt.metroll.shared.domain.dto.account.AccountDto;
import com.fpt.metroll.shared.domain.enums.AccountRole;
import com.fpt.metroll.shared.domain.mapper.PageMapper;
import com.fpt.metroll.shared.exception.NoPermissionException;
import com.fpt.metroll.shared.util.MongoHelper;
import com.fpt.metroll.shared.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Objects;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final MongoHelper mongoHelper;
    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;

    public AccountServiceImpl(MongoHelper mongoHelper,
                              AccountMapper accountMapper,
                              AccountRepository accountRepository) {
        this.mongoHelper = mongoHelper;
        this.accountMapper = accountMapper;
        this.accountRepository = accountRepository;
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

        Account account = accountMapper.toDocument(request);
        account.setActive(true);

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

}
