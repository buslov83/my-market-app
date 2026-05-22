package ru.practicum.mymarket.reactive.repository;

import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import ru.practicum.mymarket.reactive.model.Product;

public interface ProductRepositoryCustom {

    Flux<Product> findByTitleOrDescription(String search, Sort sort, long offset, int limit);
}
