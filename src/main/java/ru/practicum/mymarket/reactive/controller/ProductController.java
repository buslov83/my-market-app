package ru.practicum.mymarket.reactive.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.PagingDto;
import ru.practicum.mymarket.dto.enums.SortMode;
import ru.practicum.mymarket.reactive.service.ProductService;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

    private static final int ROW_SIZE = 3;

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/", "/items"})
    public Mono<Rendering> getProducts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortMode sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize) {
        return productService.getProducts(search, sort, pageNumber, pageSize)
                .map(page -> Rendering.view("items")
                        .modelAttribute("items", chunkIntoRowsOfThree(page.getContent()))
                        .modelAttribute("search", search)
                        .modelAttribute("sort", sort.name())
                        .modelAttribute("paging",
                                new PagingDto(pageSize, pageNumber, page.hasPrevious(), page.hasNext()))
                        .build());
    }

    private static List<List<ItemDto>> chunkIntoRowsOfThree(List<ItemDto> items) {
        List<List<ItemDto>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += ROW_SIZE) {
            rows.add(new ArrayList<>(items.subList(i, Math.min(i + ROW_SIZE, items.size()))));
        }
        if (!rows.isEmpty() && rows.getLast().size() < ROW_SIZE) {
            List<ItemDto> lastRow = rows.getLast();
            while (lastRow.size() < ROW_SIZE) {
                lastRow.add(ItemDto.PLACEHOLDER);
            }
        }
        return rows;
    }
}
