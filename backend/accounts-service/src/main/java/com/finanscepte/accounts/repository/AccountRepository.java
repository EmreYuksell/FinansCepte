package com.finanscepte.accounts.repository;

import com.finanscepte.accounts.model.Account;
import com.finanscepte.common.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AccountRepository — GenericRepository<Account, String> generic arayüzünü uygular.
 * Template Method pattern: CRUD işlemleri AbstractGenericService üzerinden yürür.
 */
@Repository
public interface AccountRepository extends GenericRepository<Account, String> {

    List<Account> findByUserId(String userId);

    List<Account> findByUserIdAndType(String userId, Account.AccountType type);
}
