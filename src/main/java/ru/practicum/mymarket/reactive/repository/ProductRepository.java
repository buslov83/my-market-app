package ru.practicum.mymarket.reactive.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.practicum.mymarket.reactive.model.Product;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long>, ProductRepositoryCustom {

    @Query("SELECT external_id FROM products WHERE external_id IS NOT NULL")
    Flux<String> findAllExternalIds();
}
