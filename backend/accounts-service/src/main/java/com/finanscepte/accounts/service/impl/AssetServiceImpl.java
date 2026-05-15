package com.finanscepte.accounts.service.impl;

import com.finanscepte.accounts.dto.AssetRequest;
import com.finanscepte.accounts.dto.AssetResponse;
import com.finanscepte.accounts.model.Asset;
import com.finanscepte.accounts.repository.AssetRepository;
import com.finanscepte.accounts.service.AssetService;
import com.finanscepte.accounts.util.AssetMapper;
import com.finanscepte.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetMapper assetMapper;

    public AssetServiceImpl(AssetRepository assetRepository, AssetMapper assetMapper) {
        this.assetRepository = assetRepository;
        this.assetMapper = assetMapper;
    }

    @Override
    public AssetResponse create(AssetRequest request) {
        Asset asset = assetMapper.toEntity(request);
        Asset saved = assetRepository.save(asset);
        return assetMapper.toResponse(saved);
    }

    @Override
    public AssetResponse update(String id, AssetRequest request) {
        Asset existing = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
        assetMapper.updateEntity(existing, request);
        Asset updated = assetRepository.save(existing);
        return assetMapper.toResponse(updated);
    }

    @Override
    public AssetResponse findById(String id) {
        return assetRepository.findById(id)
                .map(assetMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
    }

    @Override
    public List<AssetResponse> findAll() {
        return assetRepository.findAll().stream()
                .map(assetMapper::toResponse)
                .toList();
    }

    @Override
    public List<AssetResponse> findByUserId(String userId) {
        return assetRepository.findByUserId(userId).stream()
                .map(assetMapper::toResponse)
                .toList();
    }

    @Override
    public List<AssetResponse> findByUserIdAndType(String userId, String type) {
        return assetRepository.findByUserIdAndType(userId, Asset.AssetType.valueOf(type)).stream()
                .map(assetMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        if (!assetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asset", "id", id);
        }
        assetRepository.deleteById(id);
    }
}
