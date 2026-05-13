package com.finanscepte.transaction.util;

import com.finanscepte.transaction.dto.TransactionRequest;
import com.finanscepte.transaction.dto.TransactionResponse;
import com.finanscepte.transaction.model.Transaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequest request) {
        return Transaction.builder()
                .userId(request.userId())
                .productId(request.productId())
                .amount(request.amount())
                .type(request.type())
                .description(request.description())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getProductId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
