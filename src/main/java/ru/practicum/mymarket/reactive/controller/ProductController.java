package ru.practicum.mymarket.reactive.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.PagingDto;
import ru.practicum.mymarket.dto.enums.CartAction;
import ru.practicum.mymarket.dto.enums.SortMode;
import ru.practicum.mymarket.reactive.service.CartService;
import ru.practicum.mymarket.reactive.service.ProductService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Controller
public class ProductController {

    private static final int ROW_SIZE = 3;

    private final ProductService productService;
    private final CartService cartService;

    public ProductController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/items"})
    public Mono<Rendering> getProducts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortMode sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            WebSession session) {
        return productService.getProducts(search, sort, pageNumber, pageSize)
                .map(page -> Rendering.view("items")
                        .modelAttribute("items", chunkIntoRowsOfThree(page.items().stream()
                                .map(item -> item.withCount(cartService.quantity(item.id(), session)))
                                .toList()))
                        .modelAttribute("search", search)
                        .modelAttribute("sort", sort.name())
                        .modelAttribute("paging",
                                new PagingDto(pageSize, pageNumber, page.hasPrevious(), page.hasNext()))
                        .build());
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getProduct(@PathVariable long id, WebSession session) {
        return productService.getProduct(id)
                .map(item -> item.withCount(cartService.quantity(item.id(), session)))
                .map(item -> Rendering.view("item")
                        .modelAttribute("item", item)
                        .build())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @PostMapping("/items")
    public Mono<Rendering> updateCartFromShowcase(ServerWebExchange exchange, WebSession session) {
        return exchange.getFormData().flatMap(form -> {
            long id = Long.parseLong(requireNonNull(form.getFirst("id")));
            CartAction action = CartAction.valueOf(form.getFirst("action"));
            String url = UriComponentsBuilder.fromPath("/items")
                    .queryParams(form)
                    .replaceQueryParam("id")
                    .replaceQueryParam("action")
                    .build()
                    .encode()
                    .toUriString();
            return applyCartAction(id, action, session)
                    .thenReturn(Rendering.redirectTo(url).build());
        });
    }

    @PostMapping("/items/{id}")
    public Mono<Rendering> updateCartFromProductPage(
            @PathVariable long id, ServerWebExchange exchange, WebSession session) {
        return exchange.getFormData()
                .map(form -> CartAction.valueOf(form.getFirst("action")))
                .flatMap(action -> applyCartAction(id, action, session))
                .then(productService.getProduct(id))
                .map(item -> item.withCount(cartService.quantity(item.id(), session)))
                .map(item -> Rendering.view("item")
                        .modelAttribute("item", item)
                        .build())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    private Mono<Void> applyCartAction(long id, CartAction action, WebSession session) {
        return switch (action) {
            case PLUS -> cartService.plus(id, session);
            case MINUS -> cartService.minus(id, session);
            case DELETE -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
        };
    }

    private static List<List<ItemDto>> chunkIntoRowsOfThree(List<ItemDto> items) {
        List<List<ItemDto>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += ROW_SIZE) {
            rows.add(new ArrayList<>(items.subList(i, Math.min(i + ROW_SIZE, items.size()))));
        }
        if (!rows.isEmpty() && rows.getLast().size() < ROW_SIZE) {
            List<ItemDto> lastRow = rows.getLast();
            while (lastRow.size() < ROW_SIZE) {
                lastRow.add(ItemDto.PLACEHOLDER);
            }
        }
        return rows;
    }
}
