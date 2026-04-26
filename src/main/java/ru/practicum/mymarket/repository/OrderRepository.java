package ru.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mymarket.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}