package com.thriftstore.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thriftstore.entity.CartDisplayItem;
import com.thriftstore.service.CartService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

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
        // Store cart items in session temporarily for payment page
        List<CartDisplayItem> cart = cartService.getCartWithDetails(session);
        session.setAttribute("tempCart", cart);
        return "redirect:/payment";
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
}