package ru.practicum.mymarket.reactive.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.practicum.mymarket.reactive.model.OrderItem;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {
}
