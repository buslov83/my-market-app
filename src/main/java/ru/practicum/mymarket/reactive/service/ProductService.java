package ru.practicum.mymarket.reactive.service;

import reactor.core.publisher.Mono;

import java.nio.file.Path;

public interface ProductService {

    Mono<Long> loadProductsFromCsv(Path csvPath);
}
