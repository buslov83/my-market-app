package ru.practicum.mymarket.service;

import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.ProductsPageDto;
import ru.practicum.mymarket.dto.enums.SortMode;

import java.nio.file.Path;

public interface ProductService {

    Mono<Long> loadProductsFromCsv(Path csvPath);

    Mono<ProductsPageDto> getProducts(String search, SortMode sort, int pageNumber, int pageSize);

    Mono<ItemDto> getProduct(long id);
}
