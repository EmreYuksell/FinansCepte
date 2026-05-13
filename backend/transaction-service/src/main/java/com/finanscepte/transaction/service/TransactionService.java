package com.finanscepte.transaction.service;

import com.finanscepte.transaction.dto.TransactionRequest;
import com.finanscepte.transaction.dto.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse create(TransactionRequest request);

    TransactionResponse update(String id, TransactionRequest request);

    TransactionResponse findById(String id);

    List<TransactionResponse> findAll();

    List<TransactionResponse> findByUserId(String userId);

    List<TransactionResponse> findByUserIdAndType(String userId, String type);

    void deleteById(String id);
}
