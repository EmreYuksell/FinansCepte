package com.finanscepte.currency.repository;

import com.finanscepte.currency.model.PriceAlert;
import com.finanscepte.common.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceAlertRepository extends GenericRepository<PriceAlert, String> {
    List<PriceAlert> findByUserId(String userId);
    List<PriceAlert> findByUserIdAndIsActive(String userId, boolean isActive);
}
