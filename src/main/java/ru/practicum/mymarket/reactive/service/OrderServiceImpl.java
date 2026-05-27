package ru.practicum.mymarket.reactive.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.OrderDto;
import ru.practicum.mymarket.reactive.model.OrderItem;
import ru.practicum.mymarket.reactive.repository.OrderItemRepository;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderItemRepository orderItemRepository;

    public OrderServiceImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public Mono<OrderDto> getOrder(long id) {
        return orderItemRepository.findByOrderIdOrderByIdAsc(id)
                .collectList()
                .filter(items -> !items.isEmpty())
                .map(items -> toDto(id, items));
    }

    private static OrderDto toDto(long id, List<OrderItem> orderItems) {
        List<ItemDto> items = orderItems.stream()
                .map(oi -> new ItemDto(oi.getProductId(), oi.getTitle(), "", "", oi.getPrice(), oi.getQuantity()))
                .toList();
        long total = items.stream().mapToLong(i -> i.price() * i.count()).sum();
        return new OrderDto(id, items, total);
    }
}
