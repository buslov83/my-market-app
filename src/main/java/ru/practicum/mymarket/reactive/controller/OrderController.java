package ru.practicum.mymarket.reactive.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.reactive.service.OrderService;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders/{id}")
    public Mono<Rendering> getOrder(@PathVariable long id,
                                    @RequestParam(defaultValue = "false") boolean newOrder) {
        return orderService.getOrder(id)
                .map(order -> Rendering.view("order")
                        .modelAttribute("order", order)
                        .modelAttribute("newOrder", newOrder)
                        .build())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }
}
