package com.finanscepte.transaction.repository;

import com.finanscepte.common.GenericRepository;
import com.finanscepte.transaction.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends GenericRepository<Transaction, String> {

    List<Transaction> findByUserId(String userId);

    List<Transaction> findByUserIdAndType(String userId, String type);
}
