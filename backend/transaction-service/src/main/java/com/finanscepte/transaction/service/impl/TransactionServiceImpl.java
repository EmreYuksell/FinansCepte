package com.finanscepte.transaction.service.impl;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.transaction.dto.TransactionRequest;
import com.finanscepte.transaction.dto.TransactionResponse;
import com.finanscepte.transaction.model.Transaction;
import com.finanscepte.transaction.repository.TransactionRepository;
import com.finanscepte.transaction.service.TransactionService;
import com.finanscepte.transaction.util.TransactionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public TransactionResponse create(TransactionRequest request) {
        Transaction transaction = transactionMapper.toEntity(request);
        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @Override
    public TransactionResponse update(String id, TransactionRequest request) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
        existing.setAmount(request.amount());
        existing.setType(request.type());
        existing.setDescription(request.description());
        existing.setProductId(request.productId());
        existing.setUpdatedAt(LocalDateTime.now());
        Transaction updated = transactionRepository.save(existing);
        return transactionMapper.toResponse(updated);
    }

    @Override
    public TransactionResponse findById(String id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id));
    }

    @Override
    public List<TransactionResponse> findAll() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    public List<TransactionResponse> findByUserId(String userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    public List<TransactionResponse> findByUserIdAndType(String userId, String type) {
        return transactionRepository.findByUserIdAndType(userId, type).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction", "id", id);
        }
        transactionRepository.deleteById(id);
    }
}
