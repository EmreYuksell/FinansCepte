package com.finanscepte.subscription.controller;

import com.finanscepte.subscription.dto.SubscriptionRequest;
import com.finanscepte.subscription.dto.SubscriptionResponse;
import com.finanscepte.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> findAll() {
        return ResponseEntity.ok(subscriptionService.findAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionResponse>> findByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(subscriptionService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(subscriptionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> update(@PathVariable String id, @Valid @RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.update(id, request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        subscriptionService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        subscriptionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
