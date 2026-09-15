package com.marketforge.trading.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "asset_type", nullable = false, length = 20)
    private String assetType;

    @Column(nullable = false, length = 20)
    private String status;

    protected Instrument() {}

    public Instrument(String symbol, String name, String assetType, String status) {
        this.symbol = symbol;
        this.name = name;
        this.assetType = assetType;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getAssetType() {
        return assetType;
    }

    public String getStatus() {
        return status;
    }
}
