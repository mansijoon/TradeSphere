package com.marketforge.market.repository;

import com.marketforge.market.domain.MarketTickEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MarketTickRepository
        extends JpaRepository<MarketTickEntity, Long> {

    List<MarketTickEntity> findTop100ByInstrumentIdOrderByTimestampDesc(
            UUID instrumentId
    );
}
