package com.finanscepte.accounts.service;

import com.finanscepte.accounts.dto.AccountRequest;
import com.finanscepte.accounts.dto.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse create(AccountRequest request);

    AccountResponse update(String id, AccountRequest request);

    AccountResponse findById(String id);

    List<AccountResponse> findAll();

    List<AccountResponse> findByUserId(String userId);

    List<AccountResponse> findByUserIdAndType(String userId, String type);

    void deleteById(String id);
}
