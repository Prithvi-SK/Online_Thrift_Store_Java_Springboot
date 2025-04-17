package com.thriftstore.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; // Added import
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping; // Added import

import com.thriftstore.entity.CartDisplayItem;
import com.thriftstore.entity.Inventory;
import com.thriftstore.entity.ReturnItem;
import com.thriftstore.entity.User;
import com.thriftstore.repository.InventoryRepository;
import com.thriftstore.repository.ReturnItemRepository;
import com.thriftstore.service.CartService;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrdersController {

    @Autowired
    private CartService cartService;
    @Autowired
    private ReturnItemRepository returnItemRepository;
    @Autowired
    private InventoryRepository inventoryRepository; // Added injection

    @GetMapping("/orders")
    public String showOrders(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"admin".equals(user.getRole().toLowerCase())) {
            return "redirect:/";
        }
        List<ReturnItem> returnItems = returnItemRepository.findAll();
        List<CartDisplayItem> displayItems = returnItems.stream()
                .map(item -> {
                    Inventory inventory = inventoryRepository.findById(item.getItemId()).orElse(null);
                    if (inventory != null) {
                        CartDisplayItem displayItem = new CartDisplayItem();
                        displayItem.setInventory(inventory);
                        displayItem.setQuantity(item.getQuantity());
                        return displayItem;
                    }
                    return null;
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
        model.addAttribute("orders", displayItems);
        model.addAttribute("isAdmin", true);
        return "orders";
    }
}