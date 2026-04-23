package ru.practicum.mymarket.service;

import org.springframework.stereotype.Service;
import ru.practicum.mymarket.repository.ProductRepository;

@Service
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final Cart cart;

    public CartServiceImpl(ProductRepository productRepository, Cart cart) {
        this.productRepository = productRepository;
        this.cart = cart;
    }

    @Override
    public void plus(long productId) {
        if (!productRepository.existsById(productId)) {
            return;
        }
        cart.plus(productId);
    }

    @Override
    public void minus(long productId) {
        // no-op if product doesn't exist in cart
        cart.minus(productId);
    }

    @Override
    public int quantity(long productId) {
        return cart.quantity(productId);
    }
}
