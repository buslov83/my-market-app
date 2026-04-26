package ru.practicum.mymarket.service;

import ru.practicum.mymarket.dto.OrderDto;

import java.util.Optional;

public interface OrderService {

    long checkout();

    Optional<OrderDto> getOrder(long id);
}