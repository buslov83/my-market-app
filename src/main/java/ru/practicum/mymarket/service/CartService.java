package ru.practicum.mymarket.service;

import ru.practicum.mymarket.dto.CartDto;
import ru.practicum.mymarket.dto.ItemDto;

import java.util.List;

public interface CartService {

    void plus(long productId);

    void minus(long productId);

    void delete(long productId);

    int quantity(long productId);

    List<ItemDto> getCartItems();

    CartDto getCart();

    void clear();
}
