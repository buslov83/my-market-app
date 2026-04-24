package ru.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.mymarket.dto.CartDto;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.service.CartService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void getCart_rendersCartViewWithItemsAndTotal() throws Exception {
        ItemDto a = new ItemDto(1L, "Apple", "desc-a", "a.jpg", 100L, 2);
        ItemDto b = new ItemDto(2L, "Bread", "desc-b", "b.jpg", 200L, 1);
        when(cartService.getCart()).thenReturn(new CartDto(List.of(a, b), 400L));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of(a, b)))
                .andExpect(model().attribute("total", 400L));
    }

    @Test
    void getCart_emptyCart_rendersCartViewWithEmptyListAndZeroTotal() throws Exception {
        when(cartService.getCart()).thenReturn(new CartDto(List.of(), 0L));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attribute("items", List.of()))
                .andExpect(model().attribute("total", 0L));
    }
}
