package ru.practicum.mymarket.reactive.service;

import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.OrderDto;

public interface OrderService {

    Mono<OrderDto> getOrder(long id);
}
