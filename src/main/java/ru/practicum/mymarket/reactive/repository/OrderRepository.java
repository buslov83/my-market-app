package ru.practicum.mymarket.reactive.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.practicum.mymarket.reactive.model.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
