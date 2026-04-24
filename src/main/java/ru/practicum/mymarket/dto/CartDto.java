package ru.practicum.mymarket.dto;

import java.util.List;

public record CartDto(
        List<ItemDto> items,
        long total
) {
    public static final CartDto EMPTY_CART = new CartDto(List.of(), 0L);
}
