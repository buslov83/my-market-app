package ru.practicum.mymarket.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.mymarket.model.Product;

import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p.externalId from Product p where p.externalId is not null")
    Set<String> findAllExternalIds();

    @Query("""
            select p from Product p
            where lower(p.title)       like lower(concat('%', :search, '%'))
               or lower(p.description) like lower(concat('%', :search, '%'))
            """)
    Page<Product> searchByTitleOrDescription(@Param("search") String search, Pageable pageable);
}
