package ru.practicum.mymarket.reactive.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART_ATTRIBUTE = "cart";

    @Override
    public void plus(long productId, WebSession session) {
        resolveCart(session).plus(productId);
    }

    @Override
    public void minus(long productId, WebSession session) {
        resolveCart(session).minus(productId);
    }

    @Override
    public int quantity(long productId, WebSession session) {
        return resolveCart(session).quantity(productId);
    }

    private Cart resolveCart(WebSession session) {
        return (Cart) session.getAttributes().computeIfAbsent(CART_ATTRIBUTE, k -> new Cart());
    }
}
