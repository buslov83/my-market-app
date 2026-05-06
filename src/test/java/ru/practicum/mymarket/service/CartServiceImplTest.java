package ru.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.mymarket.dto.CartDto;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.model.Product;
import ru.practicum.mymarket.repository.ProductRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private Cart cart;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void plus_whenProductExists_delegatesToCart() {
        long productId = 42L;
        when(productRepository.existsById(productId)).thenReturn(true);

        cartService.plus(productId);

        verify(cart).plus(productId);
    }

    @Test
    void plus_whenProductMissing_doesNotTouchCart() {
        long productId = 999L;
        when(productRepository.existsById(productId)).thenReturn(false);

        cartService.plus(productId);

        verifyNoInteractions(cart);
    }

    @Test
    void getCart_whenEmpty_returnsEmptyCartAndDoesNotQueryRepo() {
        when(cart.entries()).thenReturn(Map.of());

        CartDto result = cartService.getCart();

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isEqualTo(0L);
        verifyNoInteractions(productRepository);
    }

    @Test
    void getCart_withMultipleProducts_preservesInsertionOrderAndSumsTotal() {
        Map<Long, Integer> entries = new LinkedHashMap<>();
        entries.put(3L, 2);
        entries.put(1L, 1);
        entries.put(2L, 4);
        when(cart.entries()).thenReturn(entries);
        when(productRepository.findAllById(entries.keySet())).thenReturn(List.of(
                product(1L, "Apple", "desc-a", "a.jpg", 100L),
                product(2L, "Bread", "desc-b", "b.jpg", 200L),
                product(3L, "Carrot", "desc-c", "c.jpg", 50L)));

        CartDto result = cartService.getCart();

        assertThat(result.items()).containsExactly(
                new ItemDto(3L, "Carrot", "desc-c", "c.jpg", 50L, 2),
                new ItemDto(1L, "Apple", "desc-a", "a.jpg", 100L, 1),
                new ItemDto(2L, "Bread", "desc-b", "b.jpg", 200L, 4));
        assertThat(result.total()).isEqualTo(50L * 2 + 100L + 200L * 4);
    }

    @Test
    void getCart_ignoresCartEntriesWhoseProductIsMissingInDb() {
        Map<Long, Integer> entries = new LinkedHashMap<>();
        entries.put(1L, 2);
        entries.put(99L, 5);
        entries.put(2L, 1);
        when(cart.entries()).thenReturn(entries);
        when(productRepository.findAllById(entries.keySet())).thenReturn(List.of(
                product(1L, "Apple", "desc-a", "a.jpg", 100L),
                product(2L, "Bread", "desc-b", "b.jpg", 200L)));

        CartDto result = cartService.getCart();

        assertThat(result.items()).containsExactly(
                new ItemDto(1L, "Apple", "desc-a", "a.jpg", 100L, 2),
                new ItemDto(2L, "Bread", "desc-b", "b.jpg", 200L, 1));
        assertThat(result.total()).isEqualTo(100L * 2 + 200L);
    }

    private static Product product(long id, String title, String description, String imgPath, long price) {
        Product p = new Product(title, description, imgPath, price);
        p.setId(id);
        return p;
    }
}
