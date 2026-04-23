package ru.practicum.mymarket.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.PagingDto;
import ru.practicum.mymarket.dto.ProductsPageDto;
import ru.practicum.mymarket.dto.enums.CartAction;
import ru.practicum.mymarket.dto.enums.SortMode;
import ru.practicum.mymarket.service.CartService;
import ru.practicum.mymarket.service.ProductService;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

    private static final int ROW_SIZE = 3;

    private final ProductService productService;
    private final CartService cartService;

    public ProductController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
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

    @PostMapping("/items")
    public String updateCartFromShowcase(
            @RequestParam long id,
            @RequestParam CartAction action,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortMode sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize) {

        applyCartAction(id, action);

        String uri = UriComponentsBuilder.fromPath("/items")
                .queryParam("search", search)
                .queryParam("sort", sort.name())
                .queryParam("pageNumber", pageNumber)
                .queryParam("pageSize", pageSize)
                .build()
                .encode()
                .toUriString();
        return "redirect:" + uri;
    }

    @GetMapping("/items/{id}")
    public String getProduct(@PathVariable long id, Model model) {
        ItemDto item = productService.getProduct(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("item", item);
        return "item";
    }

    @PostMapping("/items/{id}")
    public String updateCartFromProductPage(
            @PathVariable long id,
            @RequestParam CartAction action,
            Model model) {

        applyCartAction(id, action);

        ItemDto item = productService.getProduct(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("item", item);
        return "item";
    }

    private void applyCartAction(long id, CartAction action) {
        switch (action) {
            case PLUS -> cartService.plus(id);
            case MINUS -> cartService.minus(id);
            case DELETE -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private static List<List<ItemDto>> chunkIntoRowsOfThree(List<ItemDto> items) {
        List<List<ItemDto>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += ROW_SIZE) {
            rows.add(new ArrayList<>(items.subList(i, Math.min(i + ROW_SIZE, items.size()))));
        }
        // pad the last row with placeholders if it's not full
        if (!rows.isEmpty() && rows.getLast().size() < ROW_SIZE) {
            List<ItemDto> lastRow = rows.getLast();
            while (lastRow.size() < ROW_SIZE) {
                lastRow.add(ItemDto.placeholder());
            }
        }
        return rows;
    }
}
