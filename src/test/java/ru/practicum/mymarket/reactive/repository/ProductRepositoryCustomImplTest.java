package ru.practicum.mymarket.reactive.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.mymarket.reactive.model.Product;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ActiveProfiles("test")
public class ProductRepositoryCustomImplTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll().block();
        productRepository.saveAll(List.of(
                new Product("Red Widget", "Bright color", null, 100),
                new Product("Blue Gadget", "Premium accessory", null, 200),
                new Product("Green Thing", "Nothing to see here", null, 300),
                new Product("Sparkler", "Includes a tiny WIDGET", null, 400),
                new Product("WIDGET pro", "Top-tier item", null, 500)
        )).blockLast();
    }

    @Test
    void findByTitleOrDescription_emptySearchReturnsAllProducts() {
        List<Product> result = productRepository
                .findByTitleOrDescription("", Sort.by("price"), 0, 10)
                .collectList()
                .block();

        assertThat(result)
                .extracting(Product::getTitle)
                .containsExactly("Red Widget", "Blue Gadget", "Green Thing", "Sparkler", "WIDGET pro");
    }

    @Test
    void findByTitleOrDescription_nullSearchBehavesLikeEmptySearch() {
        List<Product> result = productRepository
                .findByTitleOrDescription(null, Sort.by("price"), 0, 10)
                .collectList()
                .block();

        assertThat(result)
                .extracting(Product::getTitle)
                .containsExactly("Red Widget", "Blue Gadget", "Green Thing", "Sparkler", "WIDGET pro");
    }

    @Test
    void findByTitleOrDescription_matchesByTitleCaseInsensitive() {
        List<Product> result = productRepository
                .findByTitleOrDescription("BLUE", Sort.by("price"), 0, 10)
                .collectList()
                .block();

        assertThat(result)
                .extracting(Product::getTitle)
                .containsExactly("Blue Gadget");
    }

    @Test
    void findByTitleOrDescription_matchesByDescriptionCaseInsensitive() {
        List<Product> result = productRepository
                .findByTitleOrDescription("TINY", Sort.by("price"), 0, 10)
                .collectList()
                .block();

        assertThat(result)
                .extracting(Product::getDescription)
                .containsExactly("Includes a tiny WIDGET");
    }

    @Test
    void findByTitleOrDescription_matchesAcrossTitleAndDescription() {
        List<Product> result = productRepository
                .findByTitleOrDescription("widget", Sort.by("price"), 0, 10)
                .collectList()
                .block();

        assertThat(result)
                .extracting(Product::getTitle)
                .containsExactly("Red Widget", "Sparkler", "WIDGET pro");
    }

    @Test
    void findByTitleOrDescription_noMatchesReturnsEmpty() {
        List<Product> result = productRepository
                .findByTitleOrDescription("zzzz", Sort.by("price"), 0, 10)
                .collectList()
                .block();

        assertThat(result).isEmpty();
    }

    @Test
    void findByTitleOrDescription_respectsOffsetAndLimit() {
        List<Product> result = productRepository
                .findByTitleOrDescription("", Sort.by("price"), 1, 2)
                .collectList()
                .block();

        assertThat(result)
                .extracting(Product::getTitle)
                .containsExactly("Blue Gadget", "Green Thing");
    }
}
