package ru.practicum.mymarket.reactive.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.reactive.repository.ProductRepository;

@Service
public class CartServiceImpl implements CartService {

    private static final String CART_ATTRIBUTE = "cart";

    private final ProductRepository productRepository;

    public CartServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Mono<Void> plus(long productId, WebSession session) {
        return productRepository.existsById(productId)
                .doOnNext(exists -> {
                    if (exists) {
                        resolveCart(session).plus(productId);
                    }
                })
                .then();
    }

    @Override
    public Mono<Void> minus(long productId, WebSession session) {
        return Mono.fromRunnable(() -> resolveCart(session).minus(productId));
    }

    @Override
    public int quantity(long productId, WebSession session) {
        // pure in-memory map lookup, no need to wrap in Mono
        return resolveCart(session).quantity(productId);
    }

    private Cart resolveCart(WebSession session) {
        return (Cart) session.getAttributes().computeIfAbsent(CART_ATTRIBUTE, k -> new Cart());
    }
}
