package ru.practicum.mymarket.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.mymarket.dto.ItemDto;
import ru.practicum.mymarket.dto.ProductsPageDto;
import ru.practicum.mymarket.dto.enums.SortMode;
import ru.practicum.mymarket.model.Product;
import ru.practicum.mymarket.repository.ProductRepository;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CartService cartService;

    public ProductServiceImpl(ProductRepository productRepository, CartService cartService) {
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Override
    public void loadProductsFromCsv(Path csvPath) {
        Set<String> alreadyLoaded = productRepository.findAllExternalIds();

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
                if (!alreadyLoaded.contains(externalId) && seenInCsv.add(externalId)) {
                    newProducts.add(product);
                }
            }
        } catch (Exception e) {
            log.error("Failed to read product catalog CSV at {}", csvPath.toAbsolutePath(), e);
            return;
        }

        if (!newProducts.isEmpty()) {
            productRepository.saveAll(newProducts);
        }
        log.info("Loaded {} new products", newProducts.size());
    }

    @Override
    public ProductsPageDto getProducts(String search, SortMode sort, int pageNumber, int pageSize) {
        // pageNumber in API is 1-based, need to convert to 0-based for Spring Data
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, toSort(sort));
        Page<Product> page = StringUtils.isBlank(search)
                ? productRepository.findAll(pageable)
                : productRepository.searchByTitleOrDescription(search.trim(), pageable);
        List<ItemDto> items = page.getContent().stream()
                .map(this::toItemDto)
                .toList();
        return new ProductsPageDto(items, page.hasPrevious(), page.hasNext());
    }

    @Override
    public Optional<ItemDto> getProduct(long id) {
        return productRepository.findById(id).map(this::toItemDto);
    }

    private static Sort toSort(SortMode sort) {
        return switch (sort) {
            case NO -> Sort.by("id").ascending();
            case ALPHA -> Sort.by(Sort.Order.asc("title").ignoreCase()).and(Sort.by("id").ascending());
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
                cartService.quantity(product.getId()));
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
