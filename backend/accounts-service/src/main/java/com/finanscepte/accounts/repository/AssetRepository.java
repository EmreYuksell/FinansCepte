package com.finanscepte.accounts.repository;

import com.finanscepte.accounts.model.Asset;
import com.finanscepte.common.repository.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AssetRepository — GenericRepository<Asset, String> generic arayüzünü uygular.
 */
@Repository
public interface AssetRepository extends GenericRepository<Asset, String> {

    List<Asset> findByUserId(String userId);

    List<Asset> findByUserIdAndType(String userId, Asset.AssetType type);
}
