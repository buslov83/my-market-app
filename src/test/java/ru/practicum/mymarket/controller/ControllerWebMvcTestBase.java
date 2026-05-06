package ru.practicum.mymarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.mymarket.service.CartService;
import ru.practicum.mymarket.service.OrderService;
import ru.practicum.mymarket.service.ProductService;

@WebMvcTest
abstract class ControllerWebMvcTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected ProductService productService;

    @MockitoBean
    protected CartService cartService;

    @MockitoBean
    protected OrderService orderService;
}
