package ru.practicum.mymarket.service;

import org.springframework.stereotype.Service;
import ru.practicum.mymarket.dto.CartDto;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.model.Product;
import ru.practicum.mymarket.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

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

    @Override
    public CartDto getCart() {
        Map<Long, Integer> cartEntries = cart.entries();
        if (cartEntries.isEmpty()) {
            return CartDto.EMPTY_CART;
        }
        Map<Long, Product> cartProducts = productRepository.findAllById(cartEntries.keySet())
                .stream()
                .collect(toMap(Product::getId, Function.identity()));
        List<ItemDto> items = new ArrayList<>(cartEntries.size());
        long total = 0L;
        for (Map.Entry<Long, Integer> entry : cartEntries.entrySet()) {
            Product product = cartProducts.get(entry.getKey());
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
}
