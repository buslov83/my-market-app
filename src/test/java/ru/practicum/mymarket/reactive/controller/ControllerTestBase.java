package ru.practicum.mymarket.reactive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.practicum.mymarket.reactive.service.CartService;
import ru.practicum.mymarket.reactive.service.ProductService;

@WebFluxTest
abstract class ControllerTestBase {

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected ProductService productService;

    @MockitoBean
    protected CartService cartService;
}
