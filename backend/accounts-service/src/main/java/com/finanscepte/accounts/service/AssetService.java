package com.finanscepte.accounts.service;

import com.finanscepte.accounts.dto.AssetRequest;
import com.finanscepte.accounts.dto.AssetResponse;

import java.util.List;

public interface AssetService {

    AssetResponse create(AssetRequest request);

    AssetResponse update(String id, AssetRequest request);

    AssetResponse findById(String id);

    List<AssetResponse> findAll();

    List<AssetResponse> findByUserId(String userId);

    List<AssetResponse> findByUserIdAndType(String userId, String type);

    void deleteById(String id);
}
