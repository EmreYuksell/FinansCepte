package com.finanscepte.accounts.controller;

import com.finanscepte.accounts.dto.AccountRequest;
import com.finanscepte.accounts.dto.AccountResponse;
import com.finanscepte.accounts.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll() {
        return ResponseEntity.ok(accountService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(accountService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponse>> findByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(accountService.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<AccountResponse>> findByUserIdAndType(
            @PathVariable String userId,
            @PathVariable String type) {
        return ResponseEntity.ok(accountService.findByUserIdAndType(userId, type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable String id, @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.ok(accountService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        accountService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
