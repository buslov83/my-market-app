package ru.practicum.mymarket.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartTest {

    @Test
    void plus_fromEmpty_setsCountToOne() {
        Cart cart = new Cart();

        cart.plus(1L);

        assertThat(cart.quantity(1L)).isEqualTo(1);
    }

    @Test
    void plus_twice_incrementsToTwo() {
        Cart cart = new Cart();

        cart.plus(1L);
        cart.plus(1L);

        assertThat(cart.quantity(1L)).isEqualTo(2);
    }

    @Test
    void minus_whenAbsent_isNoOp() {
        Cart cart = new Cart();

        cart.minus(1L);

        assertThat(cart.quantity(1L)).isEqualTo(0);
    }

    @Test
    void minus_fromOne_removesEntry() {
        Cart cart = new Cart();
        cart.plus(1L);

        cart.minus(1L);

        assertThat(cart.quantity(1L)).isEqualTo(0);
    }

    @Test
    void minus_fromTwo_decrementsToOne() {
        Cart cart = new Cart();
        cart.plus(1L);
        cart.plus(1L);

        cart.minus(1L);

        assertThat(cart.quantity(1L)).isEqualTo(1);
    }

    @Test
    void quantity_whenAbsent_returnsZero() {
        Cart cart = new Cart();

        assertThat(cart.quantity(99L)).isEqualTo(0);
    }

    @Test
    void entries_whenEmpty_returnsEmptyMap() {
        Cart cart = new Cart();

        assertThat(cart.entries()).isEmpty();
    }

    @Test
    void entries_preservesInsertionOrder() {
        Cart cart = new Cart();
        cart.plus(3L);
        cart.plus(1L);
        cart.plus(2L);
        cart.plus(1L);

        Map<Long, Integer> entries = cart.entries();

        assertThat(entries).containsExactly(
                Map.entry(3L, 1),
                Map.entry(1L, 2),
                Map.entry(2L, 1));
    }

    @Test
    void entries_returnsUnmodifiableSnapshot() {
        Cart cart = new Cart();
        cart.plus(1L);

        Map<Long, Integer> snapshot = cart.entries();
        cart.plus(1L);
        cart.plus(2L);

        assertThat(snapshot).containsExactly(Map.entry(1L, 1));
        assertThatThrownBy(() -> snapshot.put(1L, 999))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> snapshot.put(42L, 7))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
