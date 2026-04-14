package ru.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mymarket.model.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByOrderByIdAsc();
}
