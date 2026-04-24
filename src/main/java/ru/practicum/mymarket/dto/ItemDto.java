package ru.practicum.mymarket.dto;

public record ItemDto(
        long id,
        String title,
        String description,
        String imgPath,
        long price,
        int count
) {
    public static final ItemDto PLACEHOLDER = new ItemDto(-1L, "", "", "", 0L, 0);
}
