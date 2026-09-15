package com.marketforge.trading.repository;

import com.marketforge.trading.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {

    Optional<Position> findByAccountIdAndInstrumentId(
            UUID accountId,
            UUID instrumentId
    );
}
