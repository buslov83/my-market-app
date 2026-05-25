package ru.practicum.mymarket.reactive.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.server.MockWebSession;
import reactor.core.publisher.Flux;
import ru.practicum.mymarket.dto.CartDto;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.reactive.model.Product;
import ru.practicum.mymarket.reactive.repository.ProductRepository;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    private static final String CART_ATTRIBUTE = "cart";

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void plus_whenSessionEmpty_createsNewCartWithItem() {
        MockWebSession session = new MockWebSession();

        cartService.plus(1L, session).block();

        Cart stored = session.getAttribute(CART_ATTRIBUTE);
        assertThat(stored).isNotNull();
        assertThat(stored.quantity(1L)).isEqualTo(1);
    }

    @Test
    void plus_existingCart() {
        MockWebSession session = new MockWebSession();
        Cart existing = new Cart();
        existing.plus(1L);
        session.getAttributes().put(CART_ATTRIBUTE, existing);

        cartService.plus(1L, session).block();

        Cart after = session.getAttribute(CART_ATTRIBUTE);
        assertThat(after).isSameAs(existing);
        assertThat(after.quantity(1L)).isEqualTo(2);
    }

    @Test
    void minus_whenSessionEmpty_createsEmptyCart() {
        MockWebSession session = new MockWebSession();

        cartService.minus(1L, session).block();

        Cart stored = session.getAttribute(CART_ATTRIBUTE);
        assertThat(stored).isNotNull();
        assertThat(stored.quantity(1L)).isZero();
    }

    @Test
    void minus_existingCart() {
        MockWebSession session = new MockWebSession();
        Cart existing = new Cart();
        existing.plus(1L);
        existing.plus(1L);
        session.getAttributes().put(CART_ATTRIBUTE, existing);

        cartService.minus(1L, session).block();

        Cart after = session.getAttribute(CART_ATTRIBUTE);
        assertThat(after).isSameAs(existing);
        assertThat(after.quantity(1L)).isEqualTo(1);
    }

    @Test
    void quantity_whenSessionEmpty_createsEmptyCart() {
        MockWebSession session = new MockWebSession();

        int quantity = cartService.quantity(99L, session);

        Cart stored = session.getAttribute(CART_ATTRIBUTE);
        assertThat(stored).isNotNull();
        assertThat(quantity).isZero();
    }

    @Test
    void quantity_existingCart() {
        MockWebSession session = new MockWebSession();
        Cart existing = new Cart();
        existing.plus(42L);
        existing.plus(42L);
        existing.plus(42L);
        session.getAttributes().put(CART_ATTRIBUTE, existing);

        int quantity = cartService.quantity(42L, session);

        Cart after = session.getAttribute(CART_ATTRIBUTE);
        assertThat(after).isSameAs(existing);
        assertThat(quantity).isEqualTo(3);
    }

    @Test
    void getCart_emptyCart_returnsEmptyCartDto() {
        MockWebSession session = new MockWebSession();
        when(productRepository.findAllById(Set.of())).thenReturn(Flux.empty());

        CartDto cart = cartService.getCart(session).block();

        assertThat(cart).isEqualTo(new CartDto(List.of(), 0L));
    }

    @Test
    void getCart_returnsItemsInInsertionOrderWithTotal() {
        MockWebSession session = new MockWebSession();
        Cart existing = new Cart();
        existing.plus(3L);
        existing.plus(1L);
        existing.plus(2L);
        existing.plus(3L);
        existing.plus(1L);
        existing.plus(3L);
        session.getAttributes().put(CART_ATTRIBUTE, existing);

        Product p1 = product(1L, "One", "one", "img/1.jpg", 100L);
        Product p2 = product(2L, "Two", "two", "img/2.jpg", 200L);
        Product p3 = product(3L, "Three", "three", "img/3.jpg", 300L);
        // Repo returns products in an order different from the cart insertion order.
        when(productRepository.findAllById(Set.of(1L, 2L, 3L))).thenReturn(Flux.just(p2, p1, p3));

        CartDto cart = cartService.getCart(session).block();

        assertThat(cart).isNotNull();
        assertThat(cart.items()).containsExactly(
                new ItemDto(3L, "Three", "three", "img/3.jpg", 300L, 3),
                new ItemDto(1L, "One", "one", "img/1.jpg", 100L, 2),
                new ItemDto(2L, "Two", "two", "img/2.jpg", 200L, 1));
        assertThat(cart.total()).isEqualTo(300L * 3 + 100L * 2 + 200L);
    }

    @Test
    void getCart_skipsProductsMissingFromRepository() {
        MockWebSession session = new MockWebSession();
        Cart existing = new Cart();
        existing.plus(1L);
        existing.plus(2L);
        session.getAttributes().put(CART_ATTRIBUTE, existing);

        Product p1 = product(1L, "Widget", "A widget", "img/w.jpg", 199L);
        when(productRepository.findAllById(Set.of(1L, 2L))).thenReturn(Flux.just(p1));

        CartDto cart = cartService.getCart(session).block();

        assertThat(cart).isNotNull();
        assertThat(cart.items()).containsExactly(
                new ItemDto(1L, "Widget", "A widget", "img/w.jpg", 199L, 1));
        assertThat(cart.total()).isEqualTo(199L);
    }

    private static Product product(long id, String title, String description, String imgPath, long price) {
        Product p = new Product(title, description, imgPath, price);
        p.setId(id);
        return p;
    }
}
