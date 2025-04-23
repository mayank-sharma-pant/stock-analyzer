package com.stock.stockanalyzer.service;

import com.stock.stockanalyzer.dto.StockPriceDto;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Iterator;

@Service
public class AlphaVantageService {

    @Value("${alpha.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public StockPriceDto fetchLatestPrice(String symbol) {
        try {
            // Alpha Vantage API endpoint with intraday data
            String url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=" +
                    symbol + "&interval=1min&apikey=" + apiKey;

            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);

            if (!json.has("Time Series (1min)")) {
                System.err.println("API Error: " + json.toString());
                return new StockPriceDto(symbol, 0.0, "Data unavailable");
            }

            JSONObject timeSeries = json.getJSONObject("Time Series (1min)");

            Iterator<String> keys = timeSeries.keys();
            if (!keys.hasNext()) {
                return new StockPriceDto(symbol, 0.0, "No data available");
            }

            String latestTime = keys.next();
            JSONObject latestData = timeSeries.getJSONObject(latestTime);

            double price = latestData.getDouble("1. open");

            return new StockPriceDto(symbol, price, latestTime);

        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            return new StockPriceDto(symbol, 0.0, "Error fetching");
        }
    }
}
