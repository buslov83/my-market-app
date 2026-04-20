package ru.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.PagingDto;
import ru.practicum.mymarket.dto.ProductsPageDto;
import ru.practicum.mymarket.dto.enums.SortMode;
import ru.practicum.mymarket.service.ProductService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

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
        ItemDto i1 = new ItemDto(1L, "A", "A", "a.jpg", 100L, 0);
        ItemDto i2 = new ItemDto(2L, "B", "B", "b.jpg", 200L, 0);
        ItemDto i3 = new ItemDto(3L, "C", "C", "c.jpg", 300L, 0);
        ItemDto i4 = new ItemDto(4L, "D", "D", "d.jpg", 400L, 0);
        when(productService.getProducts(any(), any(), anyInt(), anyInt()))
                .thenReturn(new ProductsPageDto(List.of(i1, i2, i3, i4), false, false));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("items", List.of(
                        List.of(i1, i2, i3),
                        List.of(i4, ItemDto.placeholder(), ItemDto.placeholder())
                )));
    }
}
