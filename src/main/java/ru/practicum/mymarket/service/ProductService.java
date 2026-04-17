package ru.practicum.mymarket.service;

import java.nio.file.Path;

public interface ProductService {

    void loadProductsFromCsv(Path csvPath);
}
