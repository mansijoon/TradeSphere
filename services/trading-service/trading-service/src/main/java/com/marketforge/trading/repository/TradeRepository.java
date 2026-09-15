package com.marketforge.trading.repository;

import com.marketforge.trading.domain.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID> {

    Optional<Trade> findByInstrumentIdAndExternalTradeId(
            UUID instrumentId,
            String externalTradeId
    );

    List<Trade> findByBuyOrderIdInOrSellOrderIdInOrderByExecutedAtDesc(
            List<UUID> buyOrderIds,
            List<UUID> sellOrderIds
    );
}
