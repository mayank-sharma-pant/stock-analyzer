package com.stock.stockanalyzer.controller;

import com.stock.stockanalyzer.service.StockPriceAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor // Lombok annotation to generate constructor with required arguments
public class StockPriceAlertController {

    private final StockPriceAlertService stockPriceAlertService;

    @GetMapping("/alerts")
    public String showAlertsPage(@RequestParam("userId") Long userId, Model model) {
        model.addAttribute("userId", userId);
        return "alerts"; // Refers to alerts.html in templates/
    }

    // POST: Set price alert
    @PostMapping("/set-price-alert")
    public String setPriceAlert(@RequestParam("userId") Long userId,
                                @RequestParam("symbol") String symbol,
                                @RequestParam("targetPrice") Double targetPrice,
                                Model model) {
        try {
            stockPriceAlertService.setPriceAlert(userId, symbol, targetPrice);
            return "redirect:/portfolio-management?userId=" + userId;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to set alert: " + e.getMessage());
            return "portfolio-management";
        }
    }
}
