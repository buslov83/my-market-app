package ru.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.model.Order;
import ru.practicum.mymarket.model.OrderItem;
import ru.practicum.mymarket.repository.OrderRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CartService cartService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void checkout_buildsOrderInCartItemOrderAndClearsCart() {
        when(cartService.getCartItems()).thenReturn(List.of(
                new ItemDto(3L, "Carrot", "desc-c", "c.jpg", 50L, 2),
                new ItemDto(1L, "Apple", "desc-a", "a.jpg", 100L, 1),
                new ItemDto(2L, "Bread", "desc-b", "b.jpg", 200L, 4)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order arg = inv.getArgument(0);
            arg.setId(42L);
            return arg;
        });

        long orderId = orderService.checkout();

        assertThat(orderId).isEqualTo(42L);
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order persisted = captor.getValue();
        assertThat(persisted.getItems())
                .extracting(OrderItem::getProductId, OrderItem::getTitle, OrderItem::getPrice, OrderItem::getQuantity)
                .containsExactly(
                        tuple(3L, "Carrot", 50L, 2),
                        tuple(1L, "Apple", 100L, 1),
                        tuple(2L, "Bread", 200L, 4));
        assertThat(persisted.getItems()).allSatisfy(item -> assertThat(item.getOrder()).isSameAs(persisted));
        verify(cartService).clear();
    }

    @Test
    void checkout_whenCartItemsEmpty_throwsAndDoesNotPersistOrClear() {
        when(cartService.getCartItems()).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.checkout()).isInstanceOf(IllegalStateException.class);

        verify(orderRepository, never()).save(any(Order.class));
        verify(cartService, never()).clear();
    }
}
