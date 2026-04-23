package ru.practicum.mymarket.service;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
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

    public synchronized int quantity(long productId) {
        return quantityByProductId.getOrDefault(productId, 0);
    }
}
