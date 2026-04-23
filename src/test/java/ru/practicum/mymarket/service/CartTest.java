package ru.practicum.mymarket.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
