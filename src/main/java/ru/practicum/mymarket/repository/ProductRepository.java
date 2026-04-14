package ru.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mymarket.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
