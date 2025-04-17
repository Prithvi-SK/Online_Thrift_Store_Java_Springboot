package com.thriftstore.repository;

import com.thriftstore.entity.ReturnItem;
import com.thriftstore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    List<ReturnItem> findByUser(User user);
}