package ru.practicum.mymarket.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import ru.practicum.mymarket.model.Product;

public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final R2dbcEntityTemplate entityTemplate;

    public ProductRepositoryCustomImpl(R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Flux<Product> findByTitleOrDescription(String search, Sort sort, long offset, int limit) {
        Query query;
        if (StringUtils.isNotEmpty(search)) {
            String like = "%" + search + "%";
            query = Query.query(Criteria.where("title").like(like).ignoreCase(true)
                    .or("description").like(like).ignoreCase(true));
        } else {
            query = Query.empty();
        }
        query = query.sort(sort).offset(offset).limit(limit);
        return entityTemplate.select(Product.class).matching(query).all();
    }
}
