package com.finanscepte.accounts.service.impl;

import com.finanscepte.accounts.dto.AccountRequest;
import com.finanscepte.accounts.dto.AccountResponse;
import com.finanscepte.accounts.model.Account;
import com.finanscepte.accounts.repository.AccountRepository;
import com.finanscepte.accounts.service.AccountService;
import com.finanscepte.accounts.util.AccountMapper;
import com.finanscepte.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountServiceImpl(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    @Override
    public AccountResponse create(AccountRequest request) {
        Account account = accountMapper.toEntity(request);
        Account saved = accountRepository.save(account);
        return accountMapper.toResponse(saved);
    }

    @Override
    public AccountResponse update(String id, AccountRequest request) {
        Account existing = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        accountMapper.updateEntity(existing, request);
        Account updated = accountRepository.save(existing);
        return accountMapper.toResponse(updated);
    }

    @Override
    public AccountResponse findById(String id) {
        return accountRepository.findById(id)
                .map(accountMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }

    @Override
    public List<AccountResponse> findAll() {
        return accountRepository.findAll().stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    public List<AccountResponse> findByUserId(String userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    public List<AccountResponse> findByUserIdAndType(String userId, String type) {
        return accountRepository.findByUserIdAndType(userId, Account.AccountType.valueOf(type)).stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        if (!accountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Account", "id", id);
        }
        accountRepository.deleteById(id);
    }
}
