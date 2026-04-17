package ru.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mymarket.model.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByExternalIdNotNull();
}
