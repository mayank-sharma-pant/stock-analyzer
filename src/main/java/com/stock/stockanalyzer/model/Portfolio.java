package com.stock.stockanalyzer.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "portfolio") // Explicitly specify the table name
@Data
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    private String symbol;
    private int quantity;
    private double purchasePrice;

    // Add live price field
    private double livePrice;

    // Add profit/loss field
    private double profitLoss;
}
