package ru.practicum.mymarket.service;

import ru.practicum.mymarket.dto.ProductsPageDto;
import ru.practicum.mymarket.dto.enums.SortMode;

import java.nio.file.Path;

public interface ProductService {

    void loadProductsFromCsv(Path csvPath);

    ProductsPageDto getProducts(String search, SortMode sort, int pageNumber, int pageSize);
}
