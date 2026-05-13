package com.finanscepte.transaction.controller;

import com.finanscepte.transaction.dto.TransactionRequest;
import com.finanscepte.transaction.dto.TransactionResponse;
import com.finanscepte.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAll() {
        return ResponseEntity.ok(transactionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponse>> findByUserId(
            @PathVariable String userId,
            @RequestParam(required = false) String type) {
        if (type != null) {
            return ResponseEntity.ok(transactionService.findByUserIdAndType(userId, type));
        }
        return ResponseEntity.ok(transactionService.findByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@PathVariable String id, @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        transactionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
