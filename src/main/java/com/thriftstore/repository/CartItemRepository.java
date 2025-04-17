package com.thriftstore.repository;

import com.thriftstore.entity.CartItem;
import com.thriftstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
}