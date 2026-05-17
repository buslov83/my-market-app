package ru.practicum.mymarket.reactive.service;

import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.enums.SortMode;

import java.nio.file.Path;

public interface ProductService {

    Mono<Long> loadProductsFromCsv(Path csvPath);

    Mono<Page<ItemDto>> getProducts(String search, SortMode sort, int pageNumber, int pageSize);
}
