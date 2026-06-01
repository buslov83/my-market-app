package ru.practicum.mymarket.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart {

    private final Map<Long, Integer> quantityByProductId = new LinkedHashMap<>();

    public synchronized void plus(long productId) {
        quantityByProductId.merge(productId, 1, Integer::sum);
    }

    public synchronized void minus(long productId) {
        Integer current = quantityByProductId.get(productId);
        if (current == null) {
            return;
        }
        if (current > 1) {
            quantityByProductId.put(productId, current - 1);
        } else {
            quantityByProductId.remove(productId);
        }
    }

    public synchronized void delete(long productId) {
        quantityByProductId.remove(productId);
    }

    public synchronized int quantity(long productId) {
        return quantityByProductId.getOrDefault(productId, 0);
    }

    public synchronized Map<Long, Integer> entries() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(quantityByProductId));
    }

    public synchronized void clear() {
        quantityByProductId.clear();
    }
}
