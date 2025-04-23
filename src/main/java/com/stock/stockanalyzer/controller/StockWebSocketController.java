package com.stock.stockanalyzer.controller;

import com.stock.stockanalyzer.dto.StockPriceDto;
import com.stock.stockanalyzer.service.AlphaVantageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class StockWebSocketController {

    @Autowired
    private AlphaVantageService alphaVantageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Send stock price upon client request
    @MessageMapping("/requestStock")
    @SendTo("/topic/stock")
    public StockPriceDto onRequestStock() {
        return alphaVantageService.fetchLatestPrice("AAPL");
    }

    // Sends stock update every 10 minutes to avoid exceeding API rate limit
    @Scheduled(fixedRate = 600000)  // 600,000 milliseconds = 10 minutes
    public void sendStockUpdate() {
        StockPriceDto stock = alphaVantageService.fetchLatestPrice("AAPL");
        messagingTemplate.convertAndSend("/topic/stock", stock);
    }
}
