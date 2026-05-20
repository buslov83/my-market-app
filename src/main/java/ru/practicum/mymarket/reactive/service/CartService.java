package ru.practicum.mymarket.reactive.service;

import org.springframework.web.server.WebSession;

public interface CartService {

    void plus(long productId, WebSession session);

    void minus(long productId, WebSession session);

    int quantity(long productId, WebSession session);
}
