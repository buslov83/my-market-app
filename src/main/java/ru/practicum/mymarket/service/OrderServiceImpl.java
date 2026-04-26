package ru.practicum.mymarket.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.OrderDto;
import ru.practicum.mymarket.model.Order;
import ru.practicum.mymarket.model.OrderItem;
import ru.practicum.mymarket.repository.OrderRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(CartService cartService, OrderRepository orderRepository) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public long checkout() {
        List<ItemDto> items = cartService.getCartItems();
        if (items.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        Order order = new Order();
        for (ItemDto item : items) {
            order.addItem(new OrderItem(item.id(), item.title(), item.price(), item.count()));
        }
        order.setCreatedAt(Instant.now());
        Order saved = orderRepository.save(order);
        cartService.clear();
        return saved.getId();
    }

    @Override
    public Optional<OrderDto> getOrder(long id) {
        return orderRepository.findById(id).map(this::toDto);
    }

    @Override
    public List<OrderDto> getOrders() {
        return orderRepository.findAll(Sort.by(Sort.Order.asc("id"))).stream()
                .map(this::toDto)
                .toList();
    }

    private OrderDto toDto(Order order) {
        List<ItemDto> items = order.getItems().stream()
                .map(oi -> new ItemDto(oi.getProductId(), oi.getTitle(), "", "", oi.getPrice(), oi.getQuantity()))
                .toList();
        long total = items.stream().mapToLong(i -> i.price() * i.count()).sum();
        return new OrderDto(order.getId(), items, total);
    }
}