package ru.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.ProductsPageDto;
import ru.practicum.mymarket.dto.enums.SortMode;
import ru.practicum.mymarket.model.Product;
import ru.practicum.mymarket.repository.ProductRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        when(productRepository.findAllExternalIds()).thenReturn(Set.of("2"));

        productService.loadProductsFromCsv(csvFile);

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
    void loadProductsFromCsv_nonExistentFile_exitsNormally(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("nonexistent.csv");

        when(productRepository.findAllExternalIds()).thenReturn(Set.of());

        productService.loadProductsFromCsv(missing);

        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void getProducts_blankSearch_delegatesToFindAll() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        productService.getProducts("", SortMode.NO, 1, 5);

        verify(productRepository, never()).searchByTitleOrDescription(anyString(), any());
    }

    @Test
    void getProducts_nullSearch_delegatesToFindAll() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        productService.getProducts(null, SortMode.NO, 1, 5);

        verify(productRepository, never()).searchByTitleOrDescription(anyString(), any());
    }

    @Test
    void getProducts_nonBlankSearch_delegatesToSearchQuery() {
        when(productRepository.searchByTitleOrDescription(eq("widget"), any(Pageable.class)))
                .thenReturn(Page.empty());

        productService.getProducts("  widget  ", SortMode.NO, 1, 5);

        verify(productRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void getProducts_pageNumberAndSizeAreCorrect() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        productService.getProducts("", SortMode.NO, 1, 5);

        verify(productRepository).findAll(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
    }

    @Test
    void getProducts_sortNo_sortsByIdAsc() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        productService.getProducts("", SortMode.NO, 1, 5);

        verify(productRepository).findAll(pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        Sort.Order id = sort.getOrderFor("id");
        assertThat(id).isNotNull();
        assertThat(id.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getProducts_sortAlpha_sortsByTitleIgnoreCaseThenId() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        productService.getProducts("", SortMode.ALPHA, 1, 5);

        verify(productRepository).findAll(pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        Sort.Order title = sort.getOrderFor("title");
        Sort.Order id = sort.getOrderFor("id");
        assertThat(title).isNotNull();
        assertThat(title.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(title.isIgnoreCase()).isTrue();
        assertThat(id).isNotNull();
        assertThat(id.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getProducts_sortPrice_sortsByPriceThenId() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        productService.getProducts("", SortMode.PRICE, 1, 5);

        verify(productRepository).findAll(pageableCaptor.capture());
        Sort sort = pageableCaptor.getValue().getSort();
        Sort.Order price = sort.getOrderFor("price");
        Sort.Order id = sort.getOrderFor("id");
        assertThat(price).isNotNull();
        assertThat(price.getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(id).isNotNull();
        assertThat(id.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getProducts_mapsProductsToItemDTOs(@Mock Page<Product> page) {
        Product widget = product(10L, "Widget", "A widget", "img/w.jpg", 199L);
        Product gadget = product(11L, "Gadget", "A gadget", "img/g.jpg", 299L);
        when(page.getContent()).thenReturn(List.of(widget, gadget));
        when(page.hasPrevious()).thenReturn(false);
        when(page.hasNext()).thenReturn(false);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        ProductsPageDto result = productService.getProducts("", SortMode.NO, 1, 5);

        assertThat(result.items()).containsExactly(
                new ItemDto(10L, "Widget", "A widget", "img/w.jpg", 199L, 0),
                new ItemDto(11L, "Gadget", "A gadget", "img/g.jpg", 299L, 0));
        // TODO Verify count is set correctly when we add cart management
        assertThat(result.hasPrevious()).isFalse();
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void getProducts_setsHasPreviousAndHasNextFlags(@Mock Page<Product> page) {
        when(page.getContent()).thenReturn(List.of());
        when(page.hasPrevious()).thenReturn(true);
        when(page.hasNext()).thenReturn(true);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        ProductsPageDto result = productService.getProducts("", SortMode.NO, 2, 5);

        assertThat(result.hasPrevious()).isTrue();
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void getProduct_whenExists_returnsItemDto() {
        Product widget = product(42L, "Widget", "A widget", "img/w.jpg", 199L);
        when(productRepository.findById(42L)).thenReturn(Optional.of(widget));

        Optional<ItemDto> result = productService.getProduct(42L);

        assertThat(result).isPresent();
        ItemDto dto = result.get();
        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.title()).isEqualTo("Widget");
        assertThat(dto.description()).isEqualTo("A widget");
        assertThat(dto.imgPath()).isEqualTo("img/w.jpg");
        assertThat(dto.price()).isEqualTo(199L);
        // TODO Verify count is set correctly when we add cart management
    }

    @Test
    void getProduct_whenNotExists_returnsEmpty() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ItemDto> result = productService.getProduct(99L);

        assertThat(result).isEmpty();
    }

    private static Product product(long id, String title, String description,
                                   String imgPath, long price) {
        Product p = new Product(title, description, imgPath, price);
        p.setId(id);
        return p;
    }
}
