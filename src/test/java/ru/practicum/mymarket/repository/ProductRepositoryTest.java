package ru.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.mymarket.model.Product;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.save(new Product("Widget", "A nice thing", "img/w.jpg", 1990, "ext-1"));
        productRepository.save(new Product("Gadget", "A cool gadget", "img/g.jpg", 2500, "ext-2"));
        productRepository.save(new Product("Boomer", "Useful boomer", "img/d.jpg", 500));
    }

    @Test
    void findAllExternalIds_returnsOnlyNonNullIds() {
        Set<String> ids = productRepository.findAllExternalIds();

        assertThat(ids).containsExactlyInAnyOrder("ext-1", "ext-2");
    }

    @Test
    void findAllExternalIds_whenNoExternalIds_returnsEmptySet() {
        productRepository.deleteAll();
        productRepository.save(new Product("Solo", "No ext id", null, 100));

        assertThat(productRepository.findAllExternalIds()).isEmpty();
    }

    @Test
    void search_matchesTitle_caseInsensitive() {
        Page<Product> result = productRepository.searchByTitleOrDescription(
                "WIDGET", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Widget");
    }

    @Test
    void search_matchesDescription_caseInsensitive() {
        Page<Product> result = productRepository.searchByTitleOrDescription(
                "COOL", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Gadget");
    }

    @Test
    void search_matchesPartialToken() {
        Page<Product> result = productRepository.searchByTitleOrDescription(
                "oo", PageRequest.of(0, 10));

        // "cool gadget" (description) and "Boomer" (title) both contain "oo"
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Product::getTitle)
                .containsExactlyInAnyOrder("Gadget", "Boomer");
    }

    @Test
    void search_noMatch_returnsEmptyPage() {
        Page<Product> result = productRepository.searchByTitleOrDescription(
                "zzz", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void search_respectsPageSizeAndPageNumber() {
        // "e" appears in widgEt, gadgEt, boomEr — all three rows match
        Sort byId = Sort.by("id").ascending();
        Page<Product> page0 = productRepository.searchByTitleOrDescription(
                "e", PageRequest.of(0, 2, byId));
        Page<Product> page1 = productRepository.searchByTitleOrDescription(
                "e", PageRequest.of(1, 2, byId));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page1.getContent()).hasSize(1);
        assertThat(page0.getTotalElements()).isEqualTo(3);
    }
}
