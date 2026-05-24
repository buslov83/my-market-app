package ru.practicum.mymarket.reactive.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.server.MockWebSession;

import static org.assertj.core.api.Assertions.assertThat;

class CartServiceImplTest {

    private static final String CART_ATTRIBUTE = "cart";

    private final CartServiceImpl cartService = new CartServiceImpl();

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
}
