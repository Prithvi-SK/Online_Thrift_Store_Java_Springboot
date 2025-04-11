package com.thriftstore.controller;

import com.thriftstore.entity.CartDisplayItem;
import com.thriftstore.entity.Inventory;
import com.thriftstore.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart")
    public String showCart(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/";
        }
        try {
            model.addAttribute("cart", cartService.getCartWithDetails(session));
            return "cart";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading cart: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long itemId, @RequestParam int quantity, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/";
        }
        cartService.addToCart(session, itemId, quantity);
        return "redirect:/dashboard";
    }

    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/";
        }
        cartService.checkout(session);
        return "redirect:/return";
    }

    @GetMapping("/return")
    public String showReturnPage(Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartDisplayItem> lastCheckout = (List<CartDisplayItem>) session.getAttribute("lastCheckout");
        if (lastCheckout == null) {
            lastCheckout = new ArrayList<>();
        }
        model.addAttribute("lastCheckout", lastCheckout);
        return "return";
    }

    @PostMapping("/return/confirm")
    public String confirmReturn(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartDisplayItem> lastCheckout = (List<CartDisplayItem>) session.getAttribute("lastCheckout");
        if (lastCheckout != null && !lastCheckout.isEmpty()) {
            for (CartDisplayItem item : lastCheckout) {
                if (item.getInventory() != null) {
                    Inventory inventory = item.getInventory();
                    int newStock = inventory.getStock() + item.getQuantity();
                    inventory.setStock(newStock);
                    cartService.updateInventoryStock(inventory);
                }
            }
            session.removeAttribute("lastCheckout");
        }
        return "redirect:/return";
    }
}