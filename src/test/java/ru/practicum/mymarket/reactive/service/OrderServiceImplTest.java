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

import java.util.List;
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
        OrderItem a = orderItem(10L, 7L, 1L, "Widget", 100L, 2);
        OrderItem b = orderItem(11L, 7L, 2L, "Gadget", 250L, 1);
        OrderItem c = orderItem(12L, 7L, 3L, "Sparkler", 50L, 4);
        when(orderItemRepository.findByOrderIdOrderByIdAsc(7L))
                .thenReturn(Flux.just(a, b, c));

        OrderDto order = orderService.getOrder(7L).block();

        assertThat(order).isNotNull();
        assertThat(order.id()).isEqualTo(7L);
        assertThat(order.items()).containsExactly(
                new ItemDto(1L, "Widget", "", "", 100L, 2),
                new ItemDto(2L, "Gadget", "", "", 250L, 1),
                new ItemDto(3L, "Sparkler", "", "", 50L, 4));
        assertThat(order.totalSum()).isEqualTo(100L * 2 + 250L + 50L * 4);
    }

    @Test
    void getOrders_noItems_returnsEmptyList() {
        when(orderItemRepository.findAllByOrderByOrderIdAscIdAsc()).thenReturn(Flux.empty());

        List<OrderDto> orders = orderService.getOrders().block();

        assertThat(orders).isEmpty();
    }

    @Test
    void getOrders_singleOrder_returnsOneDto() {
        OrderItem a = orderItem(10L, 5L, 1L, "Widget", 100L, 2);
        OrderItem b = orderItem(11L, 5L, 2L, "Gadget", 250L, 1);
        when(orderItemRepository.findAllByOrderByOrderIdAscIdAsc()).thenReturn(Flux.just(a, b));

        List<OrderDto> orders = orderService.getOrders().block();

        assertThat(orders).hasSize(1);
        OrderDto only = orders.getFirst();
        assertThat(only.id()).isEqualTo(5L);
        assertThat(only.items()).containsExactly(
                new ItemDto(1L, "Widget", "", "", 100L, 2),
                new ItemDto(2L, "Gadget", "", "", 250L, 1));
        assertThat(only.totalSum()).isEqualTo(100L * 2 + 250L);
    }

    @Test
    void getOrders_groupsItemsByOrderId() {
        OrderItem a = orderItem(10L, 1L, 100L, "Apple", 50L, 3);
        OrderItem b = orderItem(11L, 1L, 200L, "Banana", 30L, 2);
        OrderItem c = orderItem(20L, 2L, 300L, "Cherry", 75L, 4);
        OrderItem d = orderItem(30L, 3L, 400L, "Date", 200L, 1);
        OrderItem e = orderItem(31L, 3L, 500L, "Elderberry", 120L, 5);
        when(orderItemRepository.findAllByOrderByOrderIdAscIdAsc())
                .thenReturn(Flux.just(a, b, c, d, e));

        List<OrderDto> orders = orderService.getOrders().block();

        assertThat(orders).hasSize(3);
        assertThat(orders).extracting(OrderDto::id).containsExactly(1L, 2L, 3L);

        assertThat(orders.get(0).items()).containsExactly(
                new ItemDto(100L, "Apple", "", "", 50L, 3),
                new ItemDto(200L, "Banana", "", "", 30L, 2));
        assertThat(orders.get(0).totalSum()).isEqualTo(50L * 3 + 30L * 2);

        assertThat(orders.get(1).items()).containsExactly(
                new ItemDto(300L, "Cherry", "", "", 75L, 4));
        assertThat(orders.get(1).totalSum()).isEqualTo(75L * 4);

        assertThat(orders.get(2).items()).containsExactly(
                new ItemDto(400L, "Date", "", "", 200L, 1),
                new ItemDto(500L, "Elderberry", "", "", 120L, 5));
        assertThat(orders.get(2).totalSum()).isEqualTo(200L + 120L * 5);
    }

    private static OrderItem orderItem(long id, long orderId, long productId, String title, long price, int quantity) {
        OrderItem item = new OrderItem(productId, title, price, quantity);
        item.setId(id);
        item.setOrderId(orderId);
        return item;
    }
}
