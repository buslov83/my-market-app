package ru.practicum.mymarket.reactive.service;

import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.OrderDto;

import java.util.List;

public interface OrderService {

    Mono<OrderDto> getOrder(long id);

    Mono<List<OrderDto>> getOrders();
}
