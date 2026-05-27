package ru.practicum.mymarket.reactive.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.CartDto;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.reactive.model.Order;
import ru.practicum.mymarket.reactive.model.OrderItem;
import ru.practicum.mymarket.reactive.model.Product;
import ru.practicum.mymarket.reactive.repository.OrderItemRepository;
import ru.practicum.mymarket.reactive.repository.OrderRepository;
import ru.practicum.mymarket.reactive.repository.ProductRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART_ATTRIBUTE = "cart";

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public CartServiceImpl(ProductRepository productRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public Mono<Void> plus(long productId, WebSession session) {
        return Mono.fromRunnable(() -> resolveCart(session).plus(productId));
    }

    @Override
    public Mono<Void> minus(long productId, WebSession session) {
        return Mono.fromRunnable(() -> resolveCart(session).minus(productId));
    }

    @Override
    public Mono<Void> delete(long productId, WebSession session) {
        return Mono.fromRunnable(() -> resolveCart(session).delete(productId));
    }

    @Override
    public int quantity(long productId, WebSession session) {
        return resolveCart(session).quantity(productId);
    }

    @Override
    public Mono<CartDto> getCart(WebSession session) {
        return Mono.fromSupplier(() -> resolveCart(session).entries())
                .flatMap(entries -> productRepository.findAllById(entries.keySet())
                        .collectMap(Product::getId)
                        .map(productsById -> buildCart(entries, productsById)));
    }

    @Override
    @Transactional
    public Mono<Long> checkout(WebSession session) {
        return getCart(session)
                .filter(cart -> !cart.items().isEmpty())
                .switchIfEmpty(Mono.error(new IllegalStateException("Cart is empty")))
                .flatMap(cart -> orderRepository.save(newOrder())
                        .flatMap(order -> orderItemRepository.saveAll(toOrderItems(cart.items(), order.getId()))
                                .then(Mono.fromRunnable(() -> resolveCart(session).clear()))
                                .thenReturn(order.getId())));
    }

    private static CartDto buildCart(Map<Long, Integer> entries, Map<Long, Product> productsById) {
        List<ItemDto> items = new ArrayList<>(entries.size());
        long total = 0L;
        for (Map.Entry<Long, Integer> entry : entries.entrySet()) {
            Product product = productsById.get(entry.getKey());
            if (product == null) {
                continue;
            }
            int count = entry.getValue();
            items.add(new ItemDto(product.getId(), product.getTitle(), product.getDescription(), product.getImgPath(),
                    product.getPrice(), count));
            total += product.getPrice() * count;
        }
        return new CartDto(items, total);
    }

    private static Order newOrder() {
        Order order = new Order();
        order.setCreatedAt(Instant.now());
        return order;
    }

    private static List<OrderItem> toOrderItems(List<ItemDto> items, Long orderId) {
        List<OrderItem> result = new ArrayList<>(items.size());
        for (ItemDto item : items) {
            OrderItem orderItem = new OrderItem(item.id(), item.title(), item.price(), item.count());
            orderItem.setOrderId(orderId);
            result.add(orderItem);
        }
        return result;
    }

    private Cart resolveCart(WebSession session) {
        return (Cart) session.getAttributes().computeIfAbsent(CART_ATTRIBUTE, k -> new Cart());
    }
}
