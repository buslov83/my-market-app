package ru.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.mymarket.model.Product;

import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p.externalId from Product p where p.externalId is not null")
    Set<String> findAllExternalIds();
}
