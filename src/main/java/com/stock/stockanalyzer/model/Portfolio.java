package com.stock.stockanalyzer.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "portfolio")
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

    private double livePrice;

    private double profitLoss;
}
