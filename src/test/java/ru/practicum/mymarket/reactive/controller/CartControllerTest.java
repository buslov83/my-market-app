package ru.practicum.mymarket.reactive.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.CartDto;
import ru.practicum.mymarket.dto.ItemDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CartControllerTest extends ControllerTestBase {

    @Test
    void getCart_rendersCartViewWithItemsAndTotal() {
        ItemDto i1 = new ItemDto(1L, "Widget", "A widget", "w.jpg", 100L, 2);
        ItemDto i2 = new ItemDto(2L, "Gadget", "A gadget", "g.jpg", 200L, 1);
        when(cartService.getCart(any(WebSession.class)))
                .thenReturn(Mono.just(new CartDto(List.of(i1, i2), 400L)));

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body)
                        .containsSubsequence("Widget", "<span>2</span>", "Gadget", "<span>1</span>")
                        .contains("Итого: 400 руб."));
    }

    @Test
    void postCart_plus_incrementsAndRendersCart() {
        ItemDto i = new ItemDto(1L, "Widget", "A widget", "w.jpg", 100L, 2);
        when(cartService.plus(anyLong(), any(WebSession.class))).thenReturn(Mono.empty());
        when(cartService.getCart(any(WebSession.class)))
                .thenReturn(Mono.just(new CartDto(List.of(i), 200L)));

        webTestClient.post().uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", "1").with("action", "PLUS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body)
                        .containsSubsequence("Widget", "<span>2</span>")
                        .contains("Итого: 200 руб."));

        verify(cartService).plus(eq(1L), any(WebSession.class));
        verify(cartService).getCart(any(WebSession.class));
        verifyNoMoreInteractions(cartService);
    }

    @Test
    void postCart_minus_decrementsAndRendersCart() {
        when(cartService.minus(anyLong(), any(WebSession.class))).thenReturn(Mono.empty());
        when(cartService.getCart(any(WebSession.class)))
                .thenReturn(Mono.just(new CartDto(List.of(), 0L)));

        webTestClient.post().uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", "1").with("action", "MINUS"))
                .exchange()
                .expectStatus().isOk();

        verify(cartService).minus(eq(1L), any(WebSession.class));
        verify(cartService).getCart(any(WebSession.class));
        verifyNoMoreInteractions(cartService);
    }

    @Test
    void postCart_delete_removesAndRendersCart() {
        when(cartService.delete(anyLong(), any(WebSession.class))).thenReturn(Mono.empty());
        when(cartService.getCart(any(WebSession.class)))
                .thenReturn(Mono.just(new CartDto(List.of(), 0L)));

        webTestClient.post().uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", "7").with("action", "DELETE"))
                .exchange()
                .expectStatus().isOk();

        verify(cartService).delete(eq(7L), any(WebSession.class));
        verify(cartService).getCart(any(WebSession.class));
        verifyNoMoreInteractions(cartService);
    }
}
