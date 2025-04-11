package com.thriftstore.service;

import com.thriftstore.entity.Inventory;
import com.thriftstore.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryFilterStrategy {

    public List<Inventory> filterByCategory(List<Inventory> inventoryList, Category category) {
        return inventoryList.stream()
                .filter(item -> item.getCategory() == category)
                .collect(Collectors.toList());
    }
}