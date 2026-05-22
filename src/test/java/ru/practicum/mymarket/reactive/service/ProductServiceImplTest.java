package ru.practicum.mymarket.reactive.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Sort> sortCaptor;

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

        Long inserted = productService.loadProductsFromCsv(csvFile).block();

        assertThat(inserted).isEqualTo(2L);

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

        Long inserted = productService.loadProductsFromCsv(missing).block();

        assertThat(inserted).isEqualTo(0L);
    }

    @Test
    void getProducts_nullSearch_forwardsEmptySearch() {
        when(productRepository.findByTitleOrDescription(eq(""), any(Sort.class), anyLong(), anyInt()))
                .thenReturn(Flux.empty());

        var page = productService.getProducts(null, SortMode.NO, 1, 5).block();
        assertThat(page).isNotNull();
        assertThat(page.items()).isEmpty();
    }

    @Test
    void getProducts_nonNullSearch_forwardsSearchTrimmed() {
        when(productRepository.findByTitleOrDescription(eq("widget"), any(Sort.class), anyLong(), anyInt()))
                .thenReturn(Flux.empty());

        var page = productService.getProducts("  widget  ", SortMode.NO, 1, 5).block();
        assertThat(page).isNotNull();
    }

    @Test
    void getProducts_pageNumberAndSize_translateToOffsetAndLimitPlusOne() {
        when(productRepository.findByTitleOrDescription(eq(""), any(Sort.class), eq(14L), eq(8)))
                .thenReturn(Flux.empty());

        var page = productService.getProducts("", SortMode.NO, 3, 7).block();
        assertThat(page).isNotNull();
    }

    @Test
    void getProducts_sortNo_sortsByIdAsc() {
        when(productRepository.findByTitleOrDescription(any(), any(Sort.class), anyLong(), anyInt()))
                .thenReturn(Flux.empty());

        productService.getProducts("", SortMode.NO, 1, 5).block();

        verify(productRepository).findByTitleOrDescription(any(), sortCaptor.capture(), anyLong(), anyInt());
        Sort sort = sortCaptor.getValue();
        Sort.Order id = sort.getOrderFor("id");
        assertThat(id).isNotNull();
        assertThat(id.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getProducts_sortAlpha_sortsByLowerTitleThenId() {
        when(productRepository.findByTitleOrDescription(any(), any(Sort.class), anyLong(), anyInt()))
                .thenReturn(Flux.empty());

        productService.getProducts("", SortMode.ALPHA, 1, 5).block();

        verify(productRepository).findByTitleOrDescription(any(), sortCaptor.capture(), anyLong(), anyInt());
        Sort sort = sortCaptor.getValue();
        assertThat(sort).extracting(Sort.Order::getProperty).containsExactly("LOWER(title)", "id");
        assertThat(sort).allSatisfy(order ->
                assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC));
    }

    @Test
    void getProducts_sortPrice_sortsByPriceThenId() {
        when(productRepository.findByTitleOrDescription(any(), any(Sort.class), anyLong(), anyInt()))
                .thenReturn(Flux.empty());

        productService.getProducts("", SortMode.PRICE, 1, 5).block();

        verify(productRepository).findByTitleOrDescription(any(), sortCaptor.capture(), anyLong(), anyInt());
        Sort sort = sortCaptor.getValue();
        assertThat(sort).extracting(Sort.Order::getProperty).containsExactly("price", "id");
        assertThat(sort).allSatisfy(order ->
                assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC));
    }

    @Test
    void getProducts_mapsProductsToItemDTOs_andTrimsToPageSize() {
        Product p1 = product(10L, "Widget", "A widget", "img/w.jpg", 199L);
        Product p2 = product(11L, "Gadget", "A gadget", "img/g.jpg", 299L);
        Product p3 = product(12L, "Ball", "A ball", "img/b.jpg", 399L);
        when(productRepository.findByTitleOrDescription(any(), any(Sort.class), eq(0L), eq(3)))
                .thenReturn(Flux.just(p1, p2, p3));

        var page = productService.getProducts("", SortMode.NO, 1, 2).block();

        assertThat(page).isNotNull();
        assertThat(page.items()).containsExactly(
                new ItemDto(10L, "Widget", "A widget", "img/w.jpg", 199L, 0),
                new ItemDto(11L, "Gadget", "A gadget", "img/g.jpg", 299L, 0));
        assertThat(page.hasPrevious()).isFalse();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void getProducts_hasNextFalseWhenFewerRowsReturned() {
        Product p1 = product(10L, "Widget", "A widget", "img/w.jpg", 199L);
        Product p2 = product(11L, "Gadget", "A gadget", "img/g.jpg", 299L);
        when(productRepository.findByTitleOrDescription(any(), any(Sort.class), eq(0L), eq(3)))
                .thenReturn(Flux.just(p1, p2));

        var page = productService.getProducts("", SortMode.NO, 1, 2).block();

        assertThat(page).isNotNull();
        assertThat(page.items()).hasSize(2);
        assertThat(page.hasNext()).isFalse();
        assertThat(page.hasPrevious()).isFalse();
    }

    @Test
    void getProducts_hasPreviousTrueFromPageTwo() {
        when(productRepository.findByTitleOrDescription(any(), any(Sort.class), anyLong(), anyInt()))
                .thenReturn(Flux.empty());

        var page = productService.getProducts("", SortMode.NO, 2, 5).block();

        assertThat(page).isNotNull();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void getProduct_whenExists_returnsItemDTO() {
        Product widget = product(42L, "Widget", "A widget", "img/w.jpg", 199L);
        when(productRepository.findById(42L)).thenReturn(Mono.just(widget));

        ItemDto dto = productService.getProduct(42L).block();

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.title()).isEqualTo("Widget");
        assertThat(dto.description()).isEqualTo("A widget");
        assertThat(dto.imgPath()).isEqualTo("img/w.jpg");
        assertThat(dto.price()).isEqualTo(199L);
        assertThat(dto.count()).isZero();
    }

    @Test
    void getProduct_whenNotExists_completesEmpty() {
        when(productRepository.findById(99L)).thenReturn(Mono.empty());

        ItemDto dto = productService.getProduct(99L).block();
        assertThat(dto).isNull();
    }

    private static Product product(long id, String title, String description,
                                   String imgPath, long price) {
        Product p = new Product(title, description, imgPath, price);
        p.setId(id);
        return p;
    }
}
