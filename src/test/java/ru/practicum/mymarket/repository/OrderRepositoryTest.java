package ru.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.mymarket.model.Order;
import ru.practicum.mymarket.model.OrderItem;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findAllWithItems_returnsOrdersByIdAsc_withItemsLoaded() {
        Order first = new Order();
        first.addItem(new OrderItem(1L, "Apple", 100L, 2));
        Order second = new Order();
        second.addItem(new OrderItem(3L, "Carrot", 50L, 1));
        second.addItem(new OrderItem(2L, "Bread", 200L, 3));
        orderRepository.save(first);
        orderRepository.save(second);

        List<Order> result = orderRepository.findAllWithItems();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Order::getId).isSorted();
        assertThat(result.get(0).getItems())
                .extracting(OrderItem::getProductId, OrderItem::getTitle, OrderItem::getPrice, OrderItem::getQuantity)
                .containsExactly(tuple(1L, "Apple", 100L, 2));
        assertThat(result.get(1).getItems())
                .extracting(OrderItem::getProductId, OrderItem::getTitle, OrderItem::getPrice, OrderItem::getQuantity)
                .containsExactly(
                        tuple(3L, "Carrot", 50L, 1),
                        tuple(2L, "Bread", 200L, 3));
    }

    @Test
    void findAllWithItems_includesOrdersWithoutItems() {
        Order empty = new Order();
        Order withItem = new Order();
        withItem.addItem(new OrderItem(1L, "Apple", 100L, 1));
        orderRepository.save(empty);
        orderRepository.save(withItem);

        List<Order> result = orderRepository.findAllWithItems();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getItems()).isEmpty();
        assertThat(result.get(1).getItems()).hasSize(1);
    }

    @Test
    void findAllWithItems_whenNoOrders_returnsEmptyList() {
        assertThat(orderRepository.findAllWithItems()).isEmpty();
    }
}
