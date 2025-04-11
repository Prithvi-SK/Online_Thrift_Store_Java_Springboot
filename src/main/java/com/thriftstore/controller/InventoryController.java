package com.thriftstore.controller;

import com.thriftstore.entity.Inventory;
import com.thriftstore.entity.User;
import com.thriftstore.entity.Category; // New import
import com.thriftstore.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping("/inventory")
    public String showInventory(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/";
        }
        List<Inventory> inventoryList = inventoryRepository.findAll();
        model.addAttribute("inventoryList", inventoryList);
        return "inventory";
    }

    @GetMapping("/editItem")
    public String editItem(@RequestParam Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/";
        }
        Inventory item = inventoryRepository.findById(id).orElse(null);
        if (item != null) {
            model.addAttribute("item", item);
            return "editItem";
        }
        return "redirect:/inventory";
    }

    @PostMapping("/updateItem")
    public String updateItem(
            @RequestParam Long id,
            @RequestParam String name,
            @RequestParam int quantity,
            @RequestParam double rentPrice,
            @RequestParam String description,
            @RequestParam String category,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/";
        }
        Inventory item = inventoryRepository.findById(id).orElse(null);
        if (item != null) {
            item.setName(name);
            item.setQuantity(quantity);
            item.setRentPrice(rentPrice);
            item.setDescription(description);
            item.setCategory(Category.valueOf(category.toUpperCase()));
            inventoryRepository.save(item);
        }
        return "redirect:/inventory";
    }

    @GetMapping("/addItem")
    public String addItemForm(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/";
        }
        return "addItem";
    }

    @PostMapping("/addItem")
    public String addItem(
            @RequestParam String name,
            @RequestParam int quantity,
            @RequestParam double rentPrice,
            @RequestParam String description,
            @RequestParam String category,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/";
        }
        Inventory newItem = new Inventory();
        newItem.setName(name);
        newItem.setQuantity(quantity);
        newItem.setRentPrice(rentPrice);
        newItem.setDescription(description);
        newItem.setCategory(Category.valueOf(category.toUpperCase()));
        inventoryRepository.save(newItem);
        return "redirect:/inventory";
    }

    @GetMapping("/removeItem")
    public String removeItem(@RequestParam Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/";
        }
        inventoryRepository.deleteById(id);
        return "redirect:/inventory";
    }
}