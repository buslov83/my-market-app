package ru.practicum.mymarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.mymarket.dto.OrderDto;
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

    @GetMapping("/orders/{id}")
    public String getOrder(
            @PathVariable long id,
            @RequestParam(defaultValue = "false") boolean newOrder,
            Model model) {
        OrderDto order = orderService.getOrder(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }
}
