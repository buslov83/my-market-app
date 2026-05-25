package ru.practicum.mymarket.reactive.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.CartDto;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.reactive.model.Product;
import ru.practicum.mymarket.reactive.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART_ATTRIBUTE = "cart";

    private final ProductRepository productRepository;

    public CartServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
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

    private Cart resolveCart(WebSession session) {
        return (Cart) session.getAttributes().computeIfAbsent(CART_ATTRIBUTE, k -> new Cart());
    }
}
