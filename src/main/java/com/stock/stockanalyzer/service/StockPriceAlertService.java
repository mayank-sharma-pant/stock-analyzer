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


    @Scheduled(fixedRate = 300000)
    public void checkAlertsAndTrigger() {
        List<StockPriceAlert> alerts = stockPriceAlertRepository.findAll();

        for (StockPriceAlert alert : alerts) {

            Double livePrice = fetchLivePrice(alert.getSymbol());


            if (livePrice != null && livePrice >= alert.getTargetPrice() && !alert.isTriggered()) {

                alert.setTriggered(true);
                stockPriceAlertRepository.save(alert);

                // Notify user (for now, print to console)
                System.out.println("Alert triggered for user " + alert.getUserId() + ": " +
                        alert.getSymbol() + " has reached " + livePrice);
            }
        }
    }


    private Double fetchLivePrice(String symbol) {
        String apiKey = "X4NXCSXGMJ02X79J";  // Replace with your actual API key
        String apiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=" + apiKey;

        try {

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder(URI.create(apiUrl)).build(), HttpResponse.BodyHandlers.ofString());


            JSONObject jsonResponse = new JSONObject(response.body());
            JSONObject timeSeries = jsonResponse.getJSONObject("Time Series (Daily)");
            String latestDate = timeSeries.keys().next();
            JSONObject latestData = timeSeries.getJSONObject(latestDate);
            return Double.parseDouble(latestData.getString("4. close"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
