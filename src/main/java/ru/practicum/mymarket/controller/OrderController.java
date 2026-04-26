package ru.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import ru.practicum.mymarket.service.OrderService;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/buy")
    public String checkout() {
        long orderId = orderService.checkout();
        return "redirect:/orders/" + orderId + "?newOrder=true";
    }
}
