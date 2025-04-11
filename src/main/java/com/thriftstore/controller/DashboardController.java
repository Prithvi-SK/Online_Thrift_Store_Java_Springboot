package com.thriftstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thriftstore.entity.Category;
import com.thriftstore.entity.Inventory;
import com.thriftstore.entity.User;
import com.thriftstore.service.CartService;
import com.thriftstore.service.InventoryFilterStrategy;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    @Autowired
    private com.thriftstore.repository.InventoryRepository inventoryRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private InventoryFilterStrategy filterStrategy;

    @GetMapping("/dashboard")
    public String showDashboard(
            Model model,
            HttpSession session,
            @RequestParam(required = false) String category) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        if (!"ADMIN".equals(user.getRole())) {
            List<Inventory> inventoryList;
            if (category != null && !category.isEmpty()) {
                inventoryList = filterStrategy.filterByCategory(inventoryRepository.findAll(), Category.valueOf(category.toUpperCase()));
            } else {
                inventoryList = inventoryRepository.findAll();
            }
            model.addAttribute("inventoryList", inventoryList);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("categories", Category.values()); // For dropdown
            return "dashboard";
        }
        return "redirect:/inventory"; // Redirect admins to inventory
    }
}