package ru.practicum.mymarket.service;

public interface CartService {

    void plus(long productId);

    void minus(long productId);

    int quantity(long productId);
}
