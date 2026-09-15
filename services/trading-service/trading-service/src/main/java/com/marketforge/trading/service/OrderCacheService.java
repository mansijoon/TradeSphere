package com.marketforge.trading.service;

import com.marketforge.trading.domain.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class OrderCacheService {

    private static final Duration TTL = Duration.ofMinutes(30);
    private static final String PREFIX = "marketforge:order:";

    private final RedisTemplate<String, Object> redisTemplate;

    public OrderCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void put(Order order) {
        if (order == null || order.getId() == null) {
            return;
        }

        redisTemplate.opsForValue().set(
                key(order.getAccountId(), order.getClientOrderId()),
                order.getId().toString(),
                TTL
        );
    }

    public UUID getOrderId(UUID accountId, String clientOrderId) {
        Object value = redisTemplate.opsForValue().get(
                key(accountId, clientOrderId)
        );

        if (value == null) {
            return null;
        }

        return UUID.fromString(value.toString());
    }

    public void evict(UUID accountId, String clientOrderId) {
        redisTemplate.delete(key(accountId, clientOrderId));
    }

    private String key(UUID accountId, String clientOrderId) {
        return PREFIX + accountId + ":" + clientOrderId;
    }
}
