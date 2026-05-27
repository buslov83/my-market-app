package ru.practicum.mymarket.reactive.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.OrderDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class OrderControllerTest extends ControllerTestBase {

    @Test
    void getOrder_rendersOrderViewWithItemsAndTotal() {
        OrderDto dto = new OrderDto(7L,
                List.of(new ItemDto(1L, "Widget", "", "", 100L, 2),
                        new ItemDto(2L, "Gadget", "", "", 250L, 1)),
                450L);
        when(orderService.getOrder(7L)).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/orders/7")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body)
                        .doesNotContain("Поздравляем! Успешная покупка!")
                        .contains("Заказ №7")
                        .containsSubsequence("Widget", "2 шт.", "100 руб.", "Сумма: 200 руб.",
                                "Gadget", "1 шт.", "250 руб.", "Сумма: 250 руб.")
                        .contains("Сумма: 450 руб."));
    }

    @Test
    void getOrder_newOrderTrue_showsCongratulationBanner() {
        OrderDto dto = new OrderDto(7L,
                List.of(new ItemDto(1L, "Widget", "", "", 100L, 2)),
                200L);
        when(orderService.getOrder(7L)).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/orders/7?newOrder=true")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Поздравляем! Успешная покупка!"));
    }

    @Test
    void getOrder_whenServiceReturnsEmptyMono_returns404() {
        when(orderService.getOrder(99L)).thenReturn(Mono.empty());

        webTestClient.get().uri("/orders/99")
                .exchange()
                .expectStatus().isNotFound();
    }
}
