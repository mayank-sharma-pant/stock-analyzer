package com.stock.stockanalyzer.controller;

import com.stock.stockanalyzer.model.Portfolio;
import com.stock.stockanalyzer.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    // GET: Portfolio Management Page
    @GetMapping("/portfolio-management")
    public String portfolioManagementPage(@RequestParam("userId") Long userId,
                                          @RequestParam(value = "error", required = false) String error,
                                          Model model) {
        List<Portfolio> portfolios = portfolioService.getPortfolio(userId);

        // Safely calculate profit/loss
        for (Portfolio portfolio : portfolios) {
            Double livePrice = portfolio.getLivePrice();
            if (livePrice != null) {
                double profitLoss = (livePrice - portfolio.getPurchasePrice()) * portfolio.getQuantity();
                portfolio.setProfitLoss(profitLoss);
            } else {
                portfolio.setProfitLoss(0.0); // Fallback if price is unavailable
            }
        }

        model.addAttribute("portfolios", portfolios);
        model.addAttribute("userId", userId);
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }

        return "portfolio-management";
    }

    // POST: Add stock to portfolio
    @PostMapping("/portfolio-management")
    public String addStockToPortfolio(@RequestParam("userId") Long userId,
                                      @RequestParam("symbol") String symbol,
                                      @RequestParam("quantity") int quantity,
                                      @RequestParam("purchasePrice") double purchasePrice,
                                      Model model) {
        try {
            // Add the stock to portfolio
            Portfolio portfolio = portfolioService.addStockToPortfolio(userId, symbol, quantity, purchasePrice);

            // Fetch updated portfolio list
            List<Portfolio> portfolios = portfolioService.getPortfolio(userId);

            // Update profit/loss after adding the stock
            for (Portfolio p : portfolios) {
                Double livePrice = p.getLivePrice();
                if (livePrice != null) {
                    double profitLoss = (livePrice - p.getPurchasePrice()) * p.getQuantity();
                    p.setProfitLoss(profitLoss);
                } else {
                    p.setProfitLoss(0.0);
                }
            }

            model.addAttribute("portfolios", portfolios);
            model.addAttribute("userId", userId);
            return "portfolio-management"; // Stay on the same page and update portfolio
        } catch (Exception e) {
            // Redirect with error message
            return "redirect:/portfolio-management?userId=" + userId + "&error=" + e.getMessage().replace(" ", "%20");
        }
    }

    // POST: Remove stock from portfolio
    @PostMapping("/remove-stock/{portfolioId}")
    public String removeStockFromPortfolio(@PathVariable("portfolioId") Long portfolioId,
                                           @RequestParam("userId") Long userId) {
        portfolioService.removeStockFromPortfolio(portfolioId);
        return "redirect:/portfolio-management?userId=" + userId;
    }

    // POST: Update existing stock in portfolio
    @PostMapping("/update-stock/{portfolioId}")
    public String updateStockInPortfolio(@PathVariable("portfolioId") Long portfolioId,
                                         @RequestParam("newQuantity") int newQuantity,
                                         @RequestParam("newPurchasePrice") double newPurchasePrice,
                                         @RequestParam("userId") Long userId) {
        portfolioService.updateStockInPortfolio(portfolioId, newQuantity, newPurchasePrice);
        return "redirect:/portfolio-management?userId=" + userId;
    }
}
