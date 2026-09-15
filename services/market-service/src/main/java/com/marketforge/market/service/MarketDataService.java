package com.marketforge.market.service;

import com.marketforge.market.domain.MarketTick;
import com.marketforge.market.domain.MarketTickEntity;
import com.marketforge.market.repository.MarketTickRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MarketDataService {

    private final MarketTickRepository repository;

    public MarketDataService(MarketTickRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MarketTickEntity record(MarketTick tick) {
        MarketTickEntity entity = new MarketTickEntity(
                tick.instrumentId(),
                tick.price(),
                tick.quantity(),
                null,
                null,
                tick.timestamp()
        );

        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public MarketTickEntity latest(UUID instrumentId) {
        return repository
                .findTop100ByInstrumentIdOrderByTimestampDesc(instrumentId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<MarketTickEntity> recent(UUID instrumentId) {
        return repository
                .findTop100ByInstrumentIdOrderByTimestampDesc(instrumentId);
    }
}
