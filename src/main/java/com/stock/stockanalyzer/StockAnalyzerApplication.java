package com.stock.stockanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockAnalyzerApplication.class, args);
	}

}
