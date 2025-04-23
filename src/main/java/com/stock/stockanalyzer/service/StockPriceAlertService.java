package com.stock.stockanalyzer.service;

import com.stock.stockanalyzer.model.StockPriceAlert;
import com.stock.stockanalyzer.repository.StockPriceAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockPriceAlertService {

    private final StockPriceAlertRepository stockPriceAlertRepository;

    // Set price alert for the user
    public StockPriceAlert setPriceAlert(Long userId, String symbol, Double targetPrice) {
        StockPriceAlert alert = new StockPriceAlert();
        alert.setUserId(userId);
        alert.setSymbol(symbol);
        alert.setTargetPrice(targetPrice);
        alert.setTriggered(false);

        return stockPriceAlertRepository.save(alert);
    }

    // Scheduled task that runs every 5 minutes (adjust the interval as needed)
    @Scheduled(fixedRate = 300000) // Runs every 5 minutes (300000ms)
    public void checkAlertsAndTrigger() {
        List<StockPriceAlert> alerts = stockPriceAlertRepository.findAll();

        for (StockPriceAlert alert : alerts) {
            // Fetch the live stock price
            Double livePrice = fetchLivePrice(alert.getSymbol());

            // Trigger the alert if conditions are met
            if (livePrice != null && livePrice >= alert.getTargetPrice() && !alert.isTriggered()) {
                // Update alert status
                alert.setTriggered(true);
                stockPriceAlertRepository.save(alert);

                // Notify user (for now, print to console)
                System.out.println("Alert triggered for user " + alert.getUserId() + ": " +
                        alert.getSymbol() + " has reached " + livePrice);
            }
        }
    }

    // Helper method to fetch live stock price (replace with actual API call)
    private Double fetchLivePrice(String symbol) {
        // Example: Implement fetching price from the Alpha Vantage API
        String apiKey = "N4UGPN9QQA7O0U72";  // Replace with your actual API key
        String apiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=" + apiKey;

        try {
            // Make the HTTP request to Alpha Vantage API (use HttpClient to fetch data)
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder(URI.create(apiUrl)).build(), HttpResponse.BodyHandlers.ofString());

            // Parse the response JSON to extract the stock price (this is a simplified example)
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONObject timeSeries = jsonResponse.getJSONObject("Time Series (Daily)");
            String latestDate = timeSeries.keys().next();
            JSONObject latestData = timeSeries.getJSONObject(latestDate);
            return Double.parseDouble(latestData.getString("4. close"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // If an error occurs, return null
        }
    }
}
