package ru.practicum.mymarket.reactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.CartDto;
import ru.practicum.mymarket.dto.enums.CartAction;
import ru.practicum.mymarket.reactive.service.CartService;

import static java.util.Objects.requireNonNull;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Mono<Rendering> getCart(WebSession session) {
        return cartService.getCart(session).map(CartController::renderCart);
    }

    @PostMapping
    public Mono<Rendering> updateCart(ServerWebExchange exchange, WebSession session) {
        return exchange.getFormData().flatMap(form -> {
            long id = Long.parseLong(requireNonNull(form.getFirst("id")));
            CartAction action = CartAction.valueOf(form.getFirst("action"));
            return applyCartAction(id, action, session)
                    .then(cartService.getCart(session))
                    .map(CartController::renderCart);
        });
    }

    private Mono<Void> applyCartAction(long id, CartAction action, WebSession session) {
        return switch (action) {
            case PLUS -> cartService.plus(id, session);
            case MINUS -> cartService.minus(id, session);
            case DELETE -> cartService.delete(id, session);
        };
    }

    private static Rendering renderCart(CartDto cart) {
        return Rendering.view("cart")
                .modelAttribute("items", cart.items())
                .modelAttribute("total", cart.total())
                .build();
    }
}
