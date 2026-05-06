package ru.practicum.mymarket.dto;

import java.util.List;

public record ProductsPageDto(
        List<ItemDto> items,
        boolean hasPrevious,
        boolean hasNext
) {
}
