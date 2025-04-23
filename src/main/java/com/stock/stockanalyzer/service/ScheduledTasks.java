package com.stock.stockanalyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduledTasks {

    private final StockPriceAlertService stockPriceAlertService;

    // Run every 30 seconds (or adjust the frequency as needed)
    @Scheduled(fixedRate = 30000)
    public void checkPriceAlerts() {
        stockPriceAlertService.checkAlertsAndTrigger();
    }
}
