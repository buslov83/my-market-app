package ru.practicum.mymarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.practicum.mymarket.service.CartService;
import ru.practicum.mymarket.service.OrderService;
import ru.practicum.mymarket.service.ProductService;

@WebFluxTest
abstract class ControllerTestBase {

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected ProductService productService;

    @MockitoBean
    protected CartService cartService;

    @MockitoBean
    protected OrderService orderService;
}
