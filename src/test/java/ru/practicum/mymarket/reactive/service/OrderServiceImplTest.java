package ru.practicum.mymarket.reactive.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.OrderDto;
import ru.practicum.mymarket.reactive.model.OrderItem;
import ru.practicum.mymarket.reactive.repository.OrderItemRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getOrder_noItems_returnsEmptyMono() {
        when(orderItemRepository.findByOrderIdOrderByIdAsc(99L)).thenReturn(Flux.empty());

        Optional<OrderDto> result = orderService.getOrder(99L).blockOptional();

        assertThat(result).isEmpty();
    }

    @Test
    void getOrder_buildsDtoFromItemsInRepositoryOrder() {
        OrderItem first = orderItem(10L, 1L, "Widget", 100L, 2);
        OrderItem second = orderItem(11L, 2L, "Gadget", 250L, 1);
        OrderItem third = orderItem(12L, 3L, "Sparkler", 50L, 4);
        when(orderItemRepository.findByOrderIdOrderByIdAsc(7L))
                .thenReturn(Flux.just(first, second, third));

        OrderDto order = orderService.getOrder(7L).block();

        assertThat(order).isNotNull();
        assertThat(order.id()).isEqualTo(7L);
        assertThat(order.items()).containsExactly(
                new ItemDto(1L, "Widget", "", "", 100L, 2),
                new ItemDto(2L, "Gadget", "", "", 250L, 1),
                new ItemDto(3L, "Sparkler", "", "", 50L, 4));
        assertThat(order.totalSum()).isEqualTo(100L * 2 + 250L + 50L * 4);
    }

    private static OrderItem orderItem(long id, long productId, String title, long price, int quantity) {
        OrderItem item = new OrderItem(productId, title, price, quantity);
        item.setId(id);
        return item;
    }
}
