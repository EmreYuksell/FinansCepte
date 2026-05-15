package com.finanscepte.accounts.service.impl;

import com.finanscepte.accounts.dto.AccountRequest;
import com.finanscepte.accounts.dto.AccountResponse;
import com.finanscepte.accounts.model.Account;
import com.finanscepte.accounts.repository.AccountRepository;
import com.finanscepte.accounts.service.AccountService;
import com.finanscepte.accounts.util.AccountMapper;
import com.finanscepte.common.AbstractGenericDtoService;
import com.finanscepte.common.GenericRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl extends AbstractGenericDtoService<AccountRequest, AccountResponse, Account, String>
        implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountServiceImpl(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    protected GenericRepository<Account, String> getRepository() {
        return accountRepository;
    }

    @Override
    protected String getEntityName() {
        return "Account";
    }

    @Override
    protected Account toEntity(AccountRequest request) {
        return accountMapper.toEntity(request);
    }

    @Override
    protected AccountResponse toResponse(Account entity) {
        return accountMapper.toResponse(entity);
    }

    @Override
    protected void applyUpdate(Account entity, AccountRequest request) {
        accountMapper.updateEntity(entity, request);
    }

    @Override
    public List<AccountResponse> findByUserId(String userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<AccountResponse> findByUserIdAndType(String userId, String type) {
        return accountRepository.findByUserIdAndType(userId, Account.AccountType.valueOf(type)).stream()
                .map(this::toResponse)
                .toList();
    }
}
