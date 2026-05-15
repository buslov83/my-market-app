package ru.practicum.mymarket.reactive.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.mymarket.reactive.model.Product;
import ru.practicum.mymarket.reactive.repository.ProductRepository;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
