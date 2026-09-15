package com.marketforge.trading.repository;

import com.marketforge.trading.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import com.marketforge.trading.domain.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByAccountIdAndClientOrderId(
            UUID accountId,
            String clientOrderId
    );

    List<Order> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    List<Order> findByAccountIdAndStatusInOrderByCreatedAtDesc(
            UUID accountId,
            List<OrderStatus> statuses
    );
}
