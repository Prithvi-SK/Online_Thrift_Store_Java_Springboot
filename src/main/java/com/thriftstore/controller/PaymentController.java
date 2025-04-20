package com.thriftstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Added import
import org.springframework.web.bind.annotation.GetMapping; // Added import
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thriftstore.entity.CartDisplayItem;
import com.thriftstore.entity.CartItem;
import com.thriftstore.entity.Inventory;
import com.thriftstore.entity.ReturnItem;
import com.thriftstore.entity.User;
import com.thriftstore.repository.CartItemRepository;
import com.thriftstore.repository.InventoryRepository;
import com.thriftstore.repository.ReturnItemRepository;
import com.thriftstore.service.CartService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaymentController {

    @Autowired
    private CartService cartService;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ReturnItemRepository returnItemRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping("/payment")
    public String showPayment(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/";
        }
        @SuppressWarnings("unchecked")
        List<CartDisplayItem> tempCart = (List<CartDisplayItem>) session.getAttribute("tempCart");
        if (tempCart == null || tempCart.isEmpty()) {
            return "redirect:/cart";
        }
        double totalPrice = tempCart.stream()
                .mapToDouble(item -> item.getInventory().getRentPrice() * item.getQuantity())
                .sum();
        model.addAttribute("cart", tempCart);
        model.addAttribute("totalPrice", totalPrice);
        return "payment";
    }

    @PostMapping("/payment/makePayment")
    public String makePayment(@RequestParam String paymentMethod, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/";
        }
        @SuppressWarnings("unchecked")
        List<CartDisplayItem> tempCart = (List<CartDisplayItem>) session.getAttribute("tempCart");
        if (tempCart == null || tempCart.isEmpty()) {
            return "redirect:/cart";
        }

        // Perform checkout
        User user = (User) session.getAttribute("user");
        for (CartDisplayItem item : tempCart) {
            ReturnItem returnItem = new ReturnItem();
            returnItem.setUser(user);
            returnItem.setItemId(item.getInventory().getId());
            returnItem.setQuantity(item.getQuantity());
            returnItemRepository.save(returnItem);

            Inventory inventory = inventoryRepository.findById(item.getInventory().getId()).orElse(null);
            if (inventory != null) {
                int newStock = inventory.getStock() - item.getQuantity();
                if (newStock >= 0) {
                    inventory.setStock(newStock);
                    inventoryRepository.save(inventory);
                }
            }

            // Remove from cart (assuming cart items are already in DB, delete them)
            CartItem cartItem = cartItemRepository.findByUserAndItemId(user, item.getInventory().getId()).orElse(null);
            if (cartItem != null) {
                cartItemRepository.delete(cartItem);
            }
        }

        // Clear temporary cart from session
        session.removeAttribute("tempCart");
        return "redirect:/return";
    }
}