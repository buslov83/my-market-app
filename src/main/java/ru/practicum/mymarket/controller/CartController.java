package ru.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.mymarket.dto.CartDto;
import ru.practicum.mymarket.dto.enums.CartAction;
import ru.practicum.mymarket.service.CartService;

@Controller
@RequestMapping("/cart/items")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String getCart(Model model) {
        populateCartModel(model);
        return "cart";
    }

    @PostMapping
    public String updateCart(@RequestParam long id, @RequestParam CartAction action, Model model) {
        switch (action) {
            case PLUS -> cartService.plus(id);
            case MINUS -> cartService.minus(id);
            case DELETE -> cartService.delete(id);
        }
        populateCartModel(model);
        return "cart";
    }

    private void populateCartModel(Model model) {
        CartDto cart = cartService.getCart();
        model.addAttribute("items", cart.items());
        model.addAttribute("total", cart.total());
    }
}