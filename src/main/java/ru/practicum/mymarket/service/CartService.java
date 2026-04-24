package ru.practicum.mymarket.service;

import ru.practicum.mymarket.dto.CartDto;

public interface CartService {

    void plus(long productId);

    void minus(long productId);

    void delete(long productId);

    int quantity(long productId);

    CartDto getCart();
}
