package com.stock.stockanalyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.stockanalyzer.model.Portfolio;
import com.stock.stockanalyzer.model.User;
import com.stock.stockanalyzer.repository.PortfolioRepository;
import com.stock.stockanalyzer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class PortfolioService {

    private static final String API_KEY = "N4UGPN9QQA7O0U72"; // Replace with your Alpha Vantage API Key
    private static final String ALPHA_VANTAGE_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=1min&apikey=%s";

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    // Add a stock to the user's portfolio
    public Portfolio addStockToPortfolio(Long userId, String symbol, int quantity, double purchasePrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolio.setSymbol(symbol);
        portfolio.setQuantity(quantity);
        portfolio.setPurchasePrice(purchasePrice);

        // Save the portfolio first
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // Fetch the live price and update the portfolio
        Double livePrice = fetchLivePrice(symbol);
        if (livePrice != null) {
            savedPortfolio.setLivePrice(livePrice);
        } else {
            savedPortfolio.setLivePrice(0.0);  // Fallback value if live price cannot be fetched
        }

        // Save with live price
        portfolioRepository.save(savedPortfolio);

        return savedPortfolio;
    }

    // View the portfolio of a user with live price and profit/loss
    public List<Portfolio> getPortfolio(Long userId) {
        List<Portfolio> portfolios = portfolioRepository.findByUserId(userId);
        for (Portfolio p : portfolios) {
            // Fetch live price for each stock
            Double livePrice = fetchLivePrice(p.getSymbol());
            p.setLivePrice(livePrice != null ? livePrice : 0.0);  // Fallback to 0.0 if live price is unavailable

            // Calculate profit/loss
            if (livePrice != null) {
                double profitLoss = (livePrice - p.getPurchasePrice()) * p.getQuantity();
                p.setProfitLoss(profitLoss);
            }
        }
        return portfolios;
    }

    // Remove a stock from the portfolio
    public void removeStockFromPortfolio(Long portfolioId) {
        Optional<Portfolio> portfolioOptional = portfolioRepository.findById(portfolioId);
        if (portfolioOptional.isPresent()) {
            portfolioRepository.delete(portfolioOptional.get());
        } else {
            throw new RuntimeException("Portfolio not found");
        }
    }

    // Update the quantity or purchase price of a stock in the portfolio
    public Portfolio updateStockInPortfolio(Long portfolioId, int newQuantity, double newPurchasePrice) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        portfolio.setQuantity(newQuantity);
        portfolio.setPurchasePrice(newPurchasePrice);

        return portfolioRepository.save(portfolio);
    }

    // Helper: Fetch live stock price from Alpha Vantage
    private Double fetchLivePrice(String symbol) {
        try {
            String requestUrl = String.format(ALPHA_VANTAGE_URL, symbol, API_KEY);
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(connection.getInputStream());

            JsonNode timeSeries = root.path("Time Series (1min)");
            if (timeSeries.isObject()) {
                Iterator<String> times = timeSeries.fieldNames();
                if (times.hasNext()) {
                    String latestTime = times.next();
                    JsonNode latestData = timeSeries.get(latestTime);
                    return latestData.path("1. open").asDouble(); // You can choose "4. close" instead
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to fetch live price for symbol: " + symbol);
        }
        return null;
    }
}
