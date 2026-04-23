package ru.practicum.mymarket.service;

import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

    private final Cart cart;

    public CartServiceImpl(Cart cart) {
        this.cart = cart;
    }

    @Override
    public void plus(long productId) {
        cart.plus(productId);
    }

    @Override
    public void minus(long productId) {
        cart.minus(productId);
    }

    @Override
    public int quantity(long productId) {
        return cart.quantity(productId);
    }
}
