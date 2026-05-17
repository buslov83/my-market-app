package ru.practicum.mymarket.reactive.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.reactive.model.Product;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    @Query("SELECT external_id FROM products WHERE external_id IS NOT NULL")
    Flux<String> findAllExternalIds();

    Flux<Product> findAllBy(Pageable pageable);

    Flux<Product> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description, Pageable pageable);

    Mono<Long> countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description);
}
