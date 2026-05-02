package ru.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.OrderDto;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerTest extends ControllerWebMvcTestBase {

    @Test
    void postBuy_redirectsToNewOrderPage() throws Exception {
        when(orderService.checkout()).thenReturn(42L);

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/42?newOrder=true"));
    }

    @Test
    void getOrder_rendersOrderViewWithNewOrderTrue() throws Exception {
        OrderDto dto = new OrderDto(7L, List.of(new ItemDto(1L, "Apple", "", "", 100L, 2)), 200L);
        when(orderService.getOrder(7L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/orders/7").param("newOrder", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", dto))
                .andExpect(model().attribute("newOrder", true));
    }

    @Test
    void getOrder_defaultsNewOrderToFalse() throws Exception {
        OrderDto dto = new OrderDto(7L, List.of(new ItemDto(1L, "Apple", "", "", 100L, 2)), 200L);
        when(orderService.getOrder(7L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/orders/7"))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attribute("order", dto))
                .andExpect(model().attribute("newOrder", false));
    }

    @Test
    void getOrder_whenServiceReturnsEmpty_returns404() throws Exception {
        when(orderService.getOrder(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrders_rendersOrdersViewWithList() throws Exception {
        OrderDto first = new OrderDto(1L, List.of(new ItemDto(1L, "Apple", "", "", 100L, 2)), 200L);
        OrderDto second = new OrderDto(2L, List.of(new ItemDto(3L, "Carrot", "", "", 50L, 1)), 50L);
        when(orderService.getOrders()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", List.of(first, second)));
    }

    @Test
    void getOrders_whenNoOrders_rendersEmptyList() throws Exception {
        when(orderService.getOrders()).thenReturn(List.of());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attribute("orders", List.of()));
    }
}
