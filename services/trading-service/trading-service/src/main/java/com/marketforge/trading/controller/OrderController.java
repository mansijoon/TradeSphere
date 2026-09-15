package com.marketforge.trading.controller;

import com.marketforge.trading.domain.Order;
import com.marketforge.trading.dto.CreateOrderRequest;
import com.marketforge.trading.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/open/{accountId}")
    public List<Order> getOpenOrders(@PathVariable UUID accountId) {
        return service.getOpenOrders(accountId);
    }

    @GetMapping("/history/{accountId}")
    public List<Order> getOrderHistory(@PathVariable UUID accountId) {
        return service.getOrderHistory(accountId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = new Order(
                request.accountId(),
                request.instrumentId(),
                request.clientOrderId(),
                request.side(),
                request.orderType(),
                request.timeInForce(),
                request.price(),
                request.quantity()
        );

        return service.create(order);
    }
}
