package ru.practicum.mymarket.reactive.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.ProductsPageDto;
import ru.practicum.mymarket.dto.enums.SortMode;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProductControllerTest extends ControllerTestBase {

    @Test
    void getRoot_rendersItemsViewWithDefaults() {
        when(productService.getProducts("", SortMode.NO, 1, 5))
                .thenReturn(Mono.just(new ProductsPageDto(List.of(), false, false)));

        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body)
                        .contains("<title>Витрина магазина</title>")
                        .contains("name=\"search\" value=\"\"")
                        .contains("<option value=\"NO\" selected=\"selected\">")
                        .contains("<option value=\"5\" selected=\"selected\">")
                        .contains("Страница: 1")
                        .doesNotContain("class=\"card\"")
                        .doesNotContain("&larr;")
                        .doesNotContain("&rarr;"));
    }

    @Test
    void getItems_rendersItemsView() {
        when(productService.getProducts("", SortMode.NO, 1, 5))
                .thenReturn(Mono.just(new ProductsPageDto(List.of(), false, false)));

        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("<title>Витрина магазина</title>"));
    }

    @Test
    void getItems_forwardsQueryParamsToServiceAndEchoesThem() {
        ItemDto i1 = new ItemDto(1L, "A", "A", "a.jpg", 100L, 0);
        ItemDto i2 = new ItemDto(2L, "B", "B", "b.jpg", 200L, 0);
        when(productService.getProducts("widget", SortMode.PRICE, 4, 2))
                .thenReturn(Mono.just(new ProductsPageDto(List.of(i1, i2), true, true)));

        webTestClient.get().uri(uri -> uri.path("/items")
                        .queryParam("search", "widget")
                        .queryParam("sort", "PRICE")
                        .queryParam("pageNumber", "4")
                        .queryParam("pageSize", "2")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body)
                        .contains("name=\"search\" value=\"widget\"")
                        .contains("<option value=\"PRICE\" selected=\"selected\">")
                        .contains("<option value=\"2\" selected=\"selected\">")
                        .contains("Страница: 4")
                        .containsSubsequence("name=\"pageNumber\" value=\"3\"", "&larr;")
                        .containsSubsequence("name=\"pageNumber\" value=\"5\"", "&rarr;"));
    }

    @Test
    void getItems_setsItemQuantity() {
        ItemDto i1 = new ItemDto(1L, "Alpha", "A", "a.jpg", 100L, 0);
        ItemDto i2 = new ItemDto(2L, "Bravo", "B", "b.jpg", 200L, 0);
        ItemDto i3 = new ItemDto(3L, "Charlie", "C", "c.jpg", 300L, 0);
        when(cartService.quantity(eq(1L), any(WebSession.class))).thenReturn(7);
        when(cartService.quantity(eq(2L), any(WebSession.class))).thenReturn(11);
        when(productService.getProducts("", SortMode.NO, 1, 5))
                .thenReturn(Mono.just(new ProductsPageDto(List.of(i1, i2, i3), false, false)));

        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).containsSubsequence(
                        "Alpha", "<span>7</span>",
                        "Bravo", "<span>11</span>",
                        "Charlie", "<span>0</span>"));
    }

    @Test
    void getItems_chunksItemsIntoRowsOfThree_padsLastRowWithPlaceholders() {
        ItemDto i1 = new ItemDto(1L, "Alpha", "A", "a.jpg", 100L, 0);
        ItemDto i2 = new ItemDto(2L, "Bravo", "B", "b.jpg", 200L, 0);
        ItemDto i3 = new ItemDto(3L, "Charlie", "C", "c.jpg", 300L, 0);
        ItemDto i4 = new ItemDto(4L, "Delta", "D", "d.jpg", 400L, 0);
        when(productService.getProducts("", SortMode.NO, 1, 5))
                .thenReturn(Mono.just(new ProductsPageDto(List.of(i1, i2, i3, i4), false, false)));

        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).containsSubsequence("Alpha", "Bravo", "Charlie", "Delta", "&nbsp;", "&nbsp;");
                    assertThat(countMatches(body, "&nbsp;")).isEqualTo(2);
                });
    }

    @Test
    void getProduct_found_rendersItemView() {
        ItemDto dto = new ItemDto(1L, "Widget", "A widget", "img/w.jpg", 199L, 0);
        when(productService.getProduct(1L)).thenReturn(Mono.just(dto));
        when(cartService.quantity(eq(1L), any(WebSession.class))).thenReturn(5);

        webTestClient.get().uri("/items/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).containsSubsequence("Widget", "<span>5</span>"));
    }

    @Test
    void getProduct_notFound_returns404() {
        when(productService.getProduct(999L)).thenReturn(Mono.empty());

        webTestClient.get().uri("/items/999")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void postItems_plus_incrementsCartAndRedirects() {
        when(cartService.plus(anyLong(), any(WebSession.class))).thenReturn(Mono.empty());

        webTestClient.post().uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", "42").with("action", "PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items");

        verify(cartService).plus(eq(42L), any(WebSession.class));
        verifyNoMoreInteractions(cartService);
    }

    @Test
    void postItems_minus_decrementsCartAndRedirects() {
        when(cartService.minus(anyLong(), any(WebSession.class))).thenReturn(Mono.empty());

        webTestClient.post().uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", "42").with("action", "MINUS"))
                .exchange()
                .expectStatus().is3xxRedirection();

        verify(cartService).minus(eq(42L), any(WebSession.class));
        verifyNoMoreInteractions(cartService);
    }

    @Test
    void postItems_redirectEchoesQueryParams() {
        when(cartService.plus(anyLong(), any(WebSession.class))).thenReturn(Mono.empty());

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("id", "7");
        form.add("action", "PLUS");
        form.add("search", "widget");
        form.add("sort", "");
        form.add("pageNumber", "3");
        form.add("pageSize", "10");

        webTestClient.post().uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location",
                        "/items?search=widget&sort=&pageNumber=3&pageSize=10");
    }

    @Test
    void postProduct_plus_incrementsCartAndRendersItemView() {
        ItemDto dto = new ItemDto(1L, "Widget", "A widget", "img/w.jpg", 199L, 0);
        when(productService.getProduct(1L)).thenReturn(Mono.just(dto));
        when(cartService.quantity(anyLong(), any(WebSession.class))).thenReturn(5);
        when(cartService.plus(anyLong(), any(WebSession.class))).thenReturn(Mono.empty());

        webTestClient.post().uri("/items/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", "PLUS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).containsSubsequence("Widget", "<span>5</span>"));

        verify(cartService).quantity(eq(1L), any(WebSession.class));
        verify(cartService).plus(eq(1L), any(WebSession.class));
        verifyNoMoreInteractions(cartService);
    }

    @Test
    void postProduct_minus_decrementsCartAndRendersItemView() {
        ItemDto dto = new ItemDto(1L, "Widget", "A widget", "img/w.jpg", 199L, 0);
        when(productService.getProduct(1L)).thenReturn(Mono.just(dto));
        when(cartService.quantity(anyLong(), any(WebSession.class))).thenReturn(5);
        when(cartService.minus(anyLong(), any(WebSession.class))).thenReturn(Mono.empty());

        webTestClient.post().uri("/items/1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", "MINUS"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).containsSubsequence("Widget", "<span>5</span>"));

        verify(cartService).quantity(eq(1L), any(WebSession.class));
        verify(cartService).minus(eq(1L), any(WebSession.class));
        verifyNoMoreInteractions(cartService);
    }
}
