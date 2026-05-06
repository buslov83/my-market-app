package ru.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.PagingDto;
import ru.practicum.mymarket.dto.ProductsPageDto;
import ru.practicum.mymarket.dto.enums.SortMode;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerTest extends ControllerWebMvcTestBase {

    @Test
    void getRoot_rendersItemsViewWithDefaultParams() throws Exception {
        when(productService.getProducts("", SortMode.NO, 1, 5))
                .thenReturn(new ProductsPageDto(List.of(), false, false));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("sort", "NO"))
                .andExpect(model().attribute("paging", new PagingDto(5, 1, false, false)))
                .andExpect(model().attribute("items", List.of()));
    }

    @Test
    void getItems_rendersItemsView() throws Exception {
        when(productService.getProducts("", SortMode.NO, 1, 5))
                .thenReturn(new ProductsPageDto(List.of(), false, false));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));
    }

    @Test
    void getItems_forwardsQueryParamsToServiceAndEchoesThem() throws Exception {
        when(productService.getProducts("widget", SortMode.PRICE, 3, 10))
                .thenReturn(new ProductsPageDto(List.of(), true, true));

        mockMvc.perform(get("/items")
                        .param("search", "widget")
                        .param("sort", "PRICE")
                        .param("pageNumber", "3")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("search", "widget"))
                .andExpect(model().attribute("sort", "PRICE"))
                .andExpect(model().attribute("paging", new PagingDto(10, 3, true, true)));
    }

    @Test
    void getItems_chunksItemsIntoRowsOfThree_padsLastRowWithPlaceholders() throws Exception {
        ItemDto i1 = new ItemDto(1L, "A", "A", "a.jpg", 100L, 1);
        ItemDto i2 = new ItemDto(2L, "B", "B", "b.jpg", 200L, 2);
        ItemDto i3 = new ItemDto(3L, "C", "C", "c.jpg", 300L, 3);
        ItemDto i4 = new ItemDto(4L, "D", "D", "d.jpg", 400L, 4);
        when(productService.getProducts(any(), any(), anyInt(), anyInt()))
                .thenReturn(new ProductsPageDto(List.of(i1, i2, i3, i4), false, false));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", List.of(
                        List.of(i1, i2, i3),
                        List.of(i4, ItemDto.PLACEHOLDER, ItemDto.PLACEHOLDER)
                )));
    }

    @Test
    void getProduct_foundProduct_rendersItemViewWithItemAttribute() throws Exception {
        ItemDto dto = new ItemDto(1L, "Widget", "A widget", "img/w.jpg", 199L, 5);
        when(productService.getProduct(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", dto));
    }

    @Test
    void getProduct_notFound_returns404() throws Exception {
        when(productService.getProduct(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/items/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postItems_plusAction_incrementsCartAndRedirects() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "42")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));

        verify(cartService).plus(42L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    void postItems_minusAction_decrementsCart() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "42")
                        .param("action", "MINUS"))
                .andExpect(status().is3xxRedirection());

        verify(cartService).minus(42L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    void postItems_redirectEchoesQueryParams() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "7")
                        .param("action", "PLUS")
                        .param("search", "widget")
                        .param("sort", "")
                        .param("pageNumber", "3")
                        .param("pageSize", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=widget&sort=&pageNumber=3&pageSize=10"));
    }

    @Test
    void postProduct_plusAction_incrementsCartAndRendersItemView() throws Exception {
        ItemDto dto = new ItemDto(1L, "Widget", "A widget", "img/w.jpg", 199L, 1);
        when(productService.getProduct(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(post("/items/1")
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attribute("item", dto));

        verify(cartService).plus(1L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    void postProduct_minusAction_decrementsCart() throws Exception {
        ItemDto dto = new ItemDto(1L, "Widget", "A widget", "img/w.jpg", 199L, 0);
        when(productService.getProduct(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(post("/items/1")
                        .param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));

        verify(cartService).minus(1L);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    void postProduct_notFound_returns404() throws Exception {
        when(productService.getProduct(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/items/999")
                        .param("action", "PLUS"))
                .andExpect(status().isNotFound());

        verify(cartService).plus(999L);
        verifyNoMoreInteractions(cartService);
    }
}
