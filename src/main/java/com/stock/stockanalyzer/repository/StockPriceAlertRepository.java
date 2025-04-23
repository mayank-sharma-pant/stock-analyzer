package com.stock.stockanalyzer.repository;

import com.stock.stockanalyzer.model.StockPriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceAlertRepository extends JpaRepository<StockPriceAlert, Long> {
}
