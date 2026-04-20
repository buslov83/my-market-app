package ru.practicum.mymarket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.PagingDto;
import ru.practicum.mymarket.dto.ProductsPageDto;
import ru.practicum.mymarket.dto.enums.SortMode;
import ru.practicum.mymarket.service.ProductService;

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
    public String getProducts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortMode sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            Model model) {

        ProductsPageDto page = productService.getProducts(search, sort, pageNumber, pageSize);

        model.addAttribute("items", chunkIntoRowsOfThree(page.items()));
        model.addAttribute("search", search);
        model.addAttribute("sort", sort.name());
        model.addAttribute("paging", new PagingDto(pageSize, pageNumber,
                page.hasPrevious(), page.hasNext()));
        return "items";
    }

    private static List<List<ItemDto>> chunkIntoRowsOfThree(List<ItemDto> items) {
        List<List<ItemDto>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += ROW_SIZE) {
            List<ItemDto> row = new ArrayList<>(items.subList(i, Math.min(i + ROW_SIZE, items.size())));
            while (row.size() < ROW_SIZE) {
                row.add(ItemDto.placeholder());
            }
            rows.add(row);
        }
        return rows;
    }
}
