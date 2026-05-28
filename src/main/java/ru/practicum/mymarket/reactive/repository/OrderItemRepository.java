package ru.practicum.mymarket.reactive.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.practicum.mymarket.reactive.model.OrderItem;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {

    Flux<OrderItem> findByOrderIdOrderByIdAsc(Long orderId);

    Flux<OrderItem> findAllByOrderByOrderIdAscIdAsc();
}
