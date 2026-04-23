package ru.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.mymarket.repository.ProductRepository;

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
}
