package ru.practicum.mymarket.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.model.Order;
import ru.practicum.mymarket.model.OrderItem;
import ru.practicum.mymarket.repository.OrderRepository;

import java.util.List;

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
        Order saved = orderRepository.save(order);
        cartService.clear();
        return saved.getId();
    }
}
