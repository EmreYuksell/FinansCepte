package com.finanscepte.accounts.controller;

import com.finanscepte.accounts.dto.AssetRequest;
import com.finanscepte.accounts.dto.AssetResponse;
import com.finanscepte.accounts.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    public ResponseEntity<AssetResponse> create(@Valid @RequestBody AssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<AssetResponse>> findAll() {
        return ResponseEntity.ok(assetService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(assetService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AssetResponse>> findByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(assetService.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<AssetResponse>> findByUserIdAndType(
            @PathVariable String userId,
            @PathVariable String type) {
        return ResponseEntity.ok(assetService.findByUserIdAndType(userId, type));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetResponse> update(@PathVariable String id, @Valid @RequestBody AssetRequest request) {
        return ResponseEntity.ok(assetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        assetService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
