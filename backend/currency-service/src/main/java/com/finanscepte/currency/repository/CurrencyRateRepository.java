package com.finanscepte.currency.repository;

import com.finanscepte.currency.model.CurrencyRate;
import com.finanscepte.common.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRateRepository extends GenericRepository<CurrencyRate, String> {
    List<CurrencyRate> findByType(String type);
    CurrencyRate findBySymbol(String symbol);
}
