package ru.practicum.mymarket.reactive.service;

import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

public interface CartService {

    Mono<Void> plus(long productId, WebSession session);

    Mono<Void> minus(long productId, WebSession session);

    int quantity(long productId, WebSession session);
}
