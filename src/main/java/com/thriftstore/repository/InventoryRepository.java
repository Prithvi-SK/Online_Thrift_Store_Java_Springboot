package com.thriftstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thriftstore.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}