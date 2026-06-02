package ru.practicum.mymarket.service;

import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.CartDto;

public interface CartService {

    Mono<Void> plus(long productId, WebSession session);

    Mono<Void> minus(long productId, WebSession session);

    Mono<Void> delete(long productId, WebSession session);

    int quantity(long productId, WebSession session);

    Mono<CartDto> getCart(WebSession session);

    Mono<Long> checkout(WebSession session);
}
