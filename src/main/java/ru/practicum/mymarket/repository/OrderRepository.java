package ru.practicum.mymarket.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.practicum.mymarket.model.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
