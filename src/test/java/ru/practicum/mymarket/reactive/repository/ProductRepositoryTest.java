package ru.practicum.mymarket.reactive.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.mymarket.reactive.model.Product;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll().block();
    }

    @Test
    void findAllExternalIds_returnsOnlyProductsWithExternalIds() {
        productRepository.saveAll(List.of(
                new Product("With ext 1", null, null, 100, "ext-1"),
                new Product("With ext 2", null, null, 200, "ext-2"),
                new Product("Without ext", null, null, 300)
        )).blockLast();

        List<String> result = productRepository.findAllExternalIds().collectList().block();

        assertThat(result).containsExactlyInAnyOrder("ext-1", "ext-2");
    }

    @Test
    void findAllExternalIds_returnsEmptyWhenNoProducts() {
        List<String> result = productRepository.findAllExternalIds().collectList().block();

        assertThat(result).isEmpty();
    }
}
