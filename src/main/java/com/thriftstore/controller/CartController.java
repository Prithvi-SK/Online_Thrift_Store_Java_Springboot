package com.thriftstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thriftstore.entity.CartDisplayItem;
import com.thriftstore.service.CartObserver;
import com.thriftstore.service.CartService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController implements CartObserver {

    @Autowired
    private CartService cartService;

    public CartController() {
        CartService.getInstance().addObserver(this);
    }

    @GetMapping("/cart")
    public String showCart(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/";
        }
        List<CartDisplayItem> cart = cartService.getCartWithDetails(session);
        model.addAttribute("cart", cart);
        return "cart";
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
        if (session.getAttribute("user") == null) {
            return "redirect:/";
        }
        List<CartDisplayItem> lastCheckout = cartService.getReturnItems(session);
        model.addAttribute("lastCheckout", lastCheckout);
        return "return";
    }

    @PostMapping("/return/confirm")
    public String confirmReturn(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/";
        }
        cartService.confirmReturn(session);
        return "redirect:/return";
    }

    @Override
    public void update(String message) {
        System.out.println("Cart Controller received update: " + message);
    }
}