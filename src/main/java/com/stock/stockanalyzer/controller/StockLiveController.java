package com.stock.stockanalyzer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StockLiveController {

    @GetMapping("/stock/live")
    public String getLiveStockPage() {
        return "live-stock";  // Make sure this matches the name of your HTML file (live-stock.html)
    }
}
