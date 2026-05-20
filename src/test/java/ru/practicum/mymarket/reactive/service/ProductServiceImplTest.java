package ru.practicum.mymarket.reactive.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.enums.SortMode;
import ru.practicum.mymarket.reactive.model.Product;
import ru.practicum.mymarket.reactive.repository.ProductRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    void loadProductsFromCsv_parsesAndFiltersCorrectly(@TempDir Path tempDir) throws IOException {
        String csv = """
                id,title,description,imgPath,price
                1,Widget, A nice widget , img/widget.jpg , 1990
                2,Old item,Already in DB,img/old.jpg,500
                ,Blank id,Blank id row,img/blank.jpg,100
                4,   ,Empty title,img/notitle.jpg,200
                5,Bad price,Non-numeric price,img/bad.jpg,abc
                6,Gadget,A cool gadget,,2500
                6,Gadget dup,Duplicate externalId,img/dup.jpg,2600
                """;
        Path csvFile = tempDir.resolve("products.csv");
        Files.writeString(csvFile, csv);

        when(productRepository.findAllExternalIds()).thenReturn(Flux.just("2"));
        when(productRepository.saveAll(anyIterable()))
                .thenAnswer(inv -> {
                    Iterable<Product> arg = inv.getArgument(0);
                    return Flux.fromIterable(arg);
                });

        StepVerifier.create(productService.loadProductsFromCsv(csvFile))
                .expectNext(2L)
                .verifyComplete();

        ArgumentCaptor<List<Product>> captor = ArgumentCaptor.captor();
        verify(productRepository).saveAll(captor.capture());

        List<Product> saved = captor.getValue();
        assertThat(saved).hasSize(2);

        Product widget = saved.get(0);
        assertThat(widget.getTitle()).isEqualTo("Widget");
        assertThat(widget.getDescription()).isEqualTo("A nice widget");
        assertThat(widget.getImgPath()).isEqualTo("img/widget.jpg");
        assertThat(widget.getPrice()).isEqualTo(1990L);
        assertThat(widget.getExternalId()).isEqualTo("1");

        Product gadget = saved.get(1);
        assertThat(gadget.getTitle()).isEqualTo("Gadget");
        assertThat(gadget.getDescription()).isEqualTo("A cool gadget");
        assertThat(gadget.getImgPath()).isNull();
        assertThat(gadget.getPrice()).isEqualTo(2500L);
        assertThat(gadget.getExternalId()).isEqualTo("6");
    }

    @Test
    void loadProductsFromCsv_nonExistentFile_completesWithZero(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("nonexistent.csv");

        when(productRepository.findAllExternalIds()).thenReturn(Flux.empty());
        when(productRepository.saveAll(anyIterable()))
                .thenAnswer(inv -> {
                    Iterable<Product> arg = inv.getArgument(0);
                    return Flux.fromIterable(arg);
                });

        StepVerifier.create(productService.loadProductsFromCsv(missing))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void getProducts_blankSearch_delegatesToFindAll() {
        when(productRepository.findAllBy(any(Pageable.class))).thenReturn(Flux.empty());
        when(productRepository.count()).thenReturn(Mono.just(0L));

        StepVerifier.create(productService.getProducts("", SortMode.NO, 1, 5))
                .expectNextCount(1)
                .verifyComplete();

        verify(productRepository, never())
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString(), any());
        verify(productRepository, never())
                .countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void getProducts_nullSearch_delegatesToFindAll() {
        when(productRepository.findAllBy(any(Pageable.class))).thenReturn(Flux.empty());
        when(productRepository.count()).thenReturn(Mono.just(0L));

        StepVerifier.create(productService.getProducts(null, SortMode.NO, 1, 5))
                .expectNextCount(1)
                .verifyComplete();

        verify(productRepository, never())
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString(), any());
    }

    @Test
    void getProducts_whitespaceSearch_delegatesToFindAll() {
        when(productRepository.findAllBy(any(Pageable.class))).thenReturn(Flux.empty());
        when(productRepository.count()).thenReturn(Mono.just(0L));

        StepVerifier.create(productService.getProducts("   ", SortMode.NO, 1, 5))
                .expectNextCount(1)
                .verifyComplete();

        verify(productRepository, never())
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString(), any());
    }

    @Test
    void getProducts_nonBlankSearch_delegatesToSearchQueryTrimmed() {
        when(productRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("widget"), eq("widget"), any(Pageable.class)))
                .thenReturn(Flux.empty());
        when(productRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("widget"), eq("widget")))
                .thenReturn(Mono.just(0L));

        StepVerifier.create(productService.getProducts("  widget  ", SortMode.NO, 1, 5))
                .expectNextCount(1)
                .verifyComplete();

        verify(productRepository, never()).findAllBy(any());
        verify(productRepository, never()).count();
    }

    @Test
    void getProducts_pageNumberAndSize() {
        when(productRepository.findAllBy(any(Pageable.class))).thenReturn(Flux.empty());
        when(productRepository.count()).thenReturn(Mono.just(0L));

        StepVerifier.create(productService.getProducts("", SortMode.NO, 1, 5))
                .expectNextCount(1)
                .verifyComplete();

        verify(productRepository).findAllBy(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
    }

    @Test
    void getProducts_sortNo_sortsByIdAsc() {
        when(productRepository.findAllBy(any(Pageable.class))).thenReturn(Flux.empty());
        when(productRepository.count()).thenReturn(Mono.just(0L));

        StepVerifier.create(productService.getProducts("", SortMode.NO, 1, 5))
                .expectNextCount(1)
                .verifyComplete();

        verify(productRepository).findAllBy(pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        Sort.Order id = sort.getOrderFor("id");
        assertThat(id).isNotNull();
        assertThat(id.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getProducts_sortAlpha_sortsByTitleIgnoreCaseThenId() {
        when(productRepository.findAllBy(any(Pageable.class))).thenReturn(Flux.empty());
        when(productRepository.count()).thenReturn(Mono.just(0L));

        StepVerifier.create(productService.getProducts("", SortMode.ALPHA, 1, 5))
                .expectNextCount(1)
                .verifyComplete();

        verify(productRepository).findAllBy(pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        assertThat(sort).extracting(Sort.Order::getProperty).containsExactly("title", "id");
        Sort.Order title = sort.getOrderFor("title");
        assertThat(title).isNotNull();
        assertThat(title.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(title.isIgnoreCase()).isTrue();
        Sort.Order id = sort.getOrderFor("id");
        assertThat(id).isNotNull();
        assertThat(id.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getProducts_sortPrice_sortsByPriceThenId() {
        when(productRepository.findAllBy(any(Pageable.class))).thenReturn(Flux.empty());
        when(productRepository.count()).thenReturn(Mono.just(0L));

        StepVerifier.create(productService.getProducts("", SortMode.PRICE, 1, 5))
                .expectNextCount(1)
                .verifyComplete();

        verify(productRepository).findAllBy(pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        assertThat(sort).extracting(Sort.Order::getProperty).containsExactly("price", "id");
        Sort.Order price = sort.getOrderFor("price");
        assertThat(price).isNotNull();
        assertThat(price.getDirection()).isEqualTo(Sort.Direction.ASC);
        Sort.Order id = sort.getOrderFor("id");
        assertThat(id).isNotNull();
        assertThat(id.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getProducts_mapsProductsToItemDTOs() {
        Product widget = product(10L, "Widget", "A widget", "img/w.jpg", 199L);
        Product gadget = product(11L, "Gadget", "A gadget", "img/g.jpg", 299L);
        when(productRepository.findAllBy(any(Pageable.class))).thenReturn(Flux.just(widget, gadget));
        when(productRepository.count()).thenReturn(Mono.just(5L));

        StepVerifier.create(productService.getProducts("", SortMode.NO, 1, 2))
                .assertNext(page -> {
                    assertThat(page.getContent()).containsExactly(
                            new ItemDto(10L, "Widget", "A widget", "img/w.jpg", 199L, 0),
                            new ItemDto(11L, "Gadget", "A gadget", "img/g.jpg", 299L, 0));
                    assertThat(page.getTotalElements()).isEqualTo(5);
                    assertThat(page.hasPrevious()).isFalse();
                    assertThat(page.hasNext()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void getProduct_whenExists_returnsItemDTO() {
        Product widget = product(42L, "Widget", "A widget", "img/w.jpg", 199L);
        when(productRepository.findById(42L)).thenReturn(Mono.just(widget));

        StepVerifier.create(productService.getProduct(42L))
                .assertNext(dto -> {
                    assertThat(dto.id()).isEqualTo(42L);
                    assertThat(dto.title()).isEqualTo("Widget");
                    assertThat(dto.description()).isEqualTo("A widget");
                    assertThat(dto.imgPath()).isEqualTo("img/w.jpg");
                    assertThat(dto.price()).isEqualTo(199L);
                    assertThat(dto.count()).isZero();
                })
                .verifyComplete();
    }

    @Test
    void getProduct_whenNotExists_completesEmpty() {
        when(productRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.getProduct(99L))
                .verifyComplete();
    }

    private static Product product(long id, String title, String description,
                                   String imgPath, long price) {
        Product p = new Product(title, description, imgPath, price);
        p.setId(id);
        return p;
    }
}
