package ru.practicum.mymarket.dto;

public record PagingDto(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
) {
}
