package com.finanscepte.goals.controller;

import com.finanscepte.goals.dto.GoalRequest;
import com.finanscepte.goals.dto.GoalResponse;
import com.finanscepte.goals.service.GoalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody GoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> findAll() {
        return ResponseEntity.ok(goalService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(goalService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GoalResponse>> findByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(goalService.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<List<GoalResponse>> findByUserIdAndCategory(
            @PathVariable String userId,
            @PathVariable String category) {
        return ResponseEntity.ok(goalService.findByUserIdAndCategory(userId, category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(@PathVariable String id, @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(goalService.update(id, request));
    }

    @PatchMapping("/{id}/deposit")
    public ResponseEntity<GoalResponse> deposit(@PathVariable String id, @RequestBody Map<String, Double> body) {
        Double amount = body.get("amount");
        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(goalService.deposit(id, amount));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        goalService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
