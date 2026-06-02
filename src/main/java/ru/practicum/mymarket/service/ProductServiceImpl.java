package ru.practicum.mymarket.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.domain.SqlSort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.ProductsPageDto;
import ru.practicum.mymarket.dto.enums.SortMode;
import ru.practicum.mymarket.model.Product;
import ru.practicum.mymarket.repository.ProductRepository;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Mono<Long> loadProductsFromCsv(Path csvPath) {
        return productRepository.findAllExternalIds()
                .collect(Collectors.toSet())
                .map(alreadyLoaded -> parseCsv(csvPath, alreadyLoaded))
                .flatMapMany(productRepository::saveAll)
                .count();
    }

    @Override
    public Mono<ProductsPageDto> getProducts(String search, SortMode sort, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        search = search != null ? search.trim() : "";
        return productRepository.findByTitleOrDescription(search, toSort(sort), offset, pageSize + 1)
                .map(this::toItemDto)
                .collectList()
                .map(items -> {
                    boolean hasNext = items.size() > pageSize;
                    List<ItemDto> page = hasNext ? items.subList(0, pageSize) : items;
                    return new ProductsPageDto(page, pageNumber > 1, hasNext);
                });
    }

    @Override
    public Mono<ItemDto> getProduct(long id) {
        return productRepository.findById(id).map(this::toItemDto);
    }

    private static Sort toSort(SortMode sort) {
        return switch (sort) {
            case NO -> Sort.by("id").ascending();
            case ALPHA -> SqlSort.unsafe("LOWER(title)").ascending()
                    .and(Sort.by("id").ascending());
            case PRICE -> Sort.by("price", "id").ascending();
        };
    }

    private ItemDto toItemDto(Product product) {
        return new ItemDto(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getImgPath(),
                product.getPrice(),
                0);
    }

    private List<Product> parseCsv(Path csvPath, Set<String> excludeIds) {
        List<Product> newProducts = new ArrayList<>();
        Set<String> seenInCsv = new HashSet<>();

        try (Reader reader = Files.newBufferedReader(csvPath);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setNullString("")
                     .build()
                     .parse(reader)) {
            for (CSVRecord record : parser) {
                Product product = parseRow(record);
                if (product == null) {
                    continue;
                }
                String externalId = product.getExternalId();
                if (!excludeIds.contains(externalId) && seenInCsv.add(externalId)) {
                    newProducts.add(product);
                }
            }
        } catch (Exception e) {
            log.error("Failed to read product catalog CSV at {}", csvPath.toAbsolutePath(), e);
            return List.of();
        }

        return newProducts;
    }

    private Product parseRow(CSVRecord record) {
        try {
            String id = record.get("id");
            String title = record.get("title");
            String description = record.get("description");
            String imgPath = record.get("imgPath");
            long price = Long.parseLong(record.get("price"));
            if (id == null || title == null) {
                log.warn("CSV row {} is missing required id or title — skipping", record.getRecordNumber());
                return null;
            }
            return new Product(title, description, imgPath, price, id);
        } catch (Exception e) {
            log.warn("CSV row {} is malformed: {} — skipping", record.getRecordNumber(), e.getMessage());
            return null;
        }
    }
}
