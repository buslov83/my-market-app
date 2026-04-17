package ru.practicum.mymarket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.practicum.mymarket.service.ProductService;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
public class MyMarketAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyMarketAppApplication.class, args);
    }
}

@Component
@ConditionalOnProperty(name = "app.catalog.load-on-startup", havingValue = "true", matchIfMissing = true)
class ProductCatalogLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductCatalogLoader.class);

    private final ProductService productService;
    private final String csvPath;

    ProductCatalogLoader(ProductService productService,
                         @Value("${app.catalog.csv-path}") String csvPath) {
        this.productService = productService;
        this.csvPath = csvPath;
    }

    @Override
    public void run(String... args) {
        Path path = Path.of(csvPath);
        if (!Files.exists(path)) {
            log.warn("Product catalog CSV not found at {} — skipping catalog load", path.toAbsolutePath());
            return;
        }
        productService.loadProductsFromCsv(path);
    }
}
