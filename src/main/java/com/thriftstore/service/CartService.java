package com.thriftstore.service;

import com.thriftstore.entity.CartItem;
import com.thriftstore.entity.Inventory;
import com.thriftstore.entity.User;
import com.thriftstore.entity.CartDisplayItem;
import com.thriftstore.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final String CART_SESSION_ATTRIBUTE = "cart_%s";
    private static final String LAST_CHECKOUT_ATTRIBUTE = "lastCheckout";
    @Autowired
    private InventoryRepository inventoryRepository;

    public List<CartDisplayItem> getCartWithDetails(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getEmail() == null) return new ArrayList<>();
        String cartKey = String.format(CART_SESSION_ATTRIBUTE, user.getEmail());
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute(cartKey);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(cartKey, cart);
        }
        return cart.stream()
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
    }

    public void addToCart(HttpSession session, Long itemId, int quantity) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getEmail() == null) return;
        List<CartItem> cart = getCart(session);
        Inventory item = inventoryRepository.findById(itemId).orElse(null);
        if (item == null || quantity <= 0 || quantity > item.getStock()) return;

        CartItem existingItem = cart.stream()
                .filter(cartItem -> cartItem.getItemId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity <= item.getStock()) {
                existingItem.setQuantity(newQuantity);
            }
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setItemId(itemId);
            cartItem.setQuantity(quantity);
            cartItem.setUserEmail(user.getEmail());
            cart.add(cartItem);
        }
    }

    private List<CartItem> getCart(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getEmail() == null) return new ArrayList<>();
        String cartKey = String.format(CART_SESSION_ATTRIBUTE, user.getEmail());
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute(cartKey);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(cartKey, cart);
        }
        return cart;
    }

    public void checkout(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getEmail() == null) return;
        String cartKey = String.format(CART_SESSION_ATTRIBUTE, user.getEmail());
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute(cartKey);
        if (cart == null || cart.isEmpty()) return;

        // Get existing checkout items or initialize a new list
        @SuppressWarnings("unchecked")
        List<CartDisplayItem> lastCheckout = (List<CartDisplayItem>) session.getAttribute(LAST_CHECKOUT_ATTRIBUTE);
        if (lastCheckout == null) {
            lastCheckout = new ArrayList<>();
        }

        // Convert current cart to checkout items
        List<CartDisplayItem> checkoutItems = cart.stream()
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

        // Append new checkout items to the existing list
        lastCheckout.addAll(checkoutItems);

        // Update session with the combined list
        session.setAttribute(LAST_CHECKOUT_ATTRIBUTE, lastCheckout);

        // Decrease inventory stock for the new checkout items
        for (CartItem item : cart) {
            Inventory inventoryItem = inventoryRepository.findById(item.getItemId()).orElse(null);
            if (inventoryItem != null) {
                int newStock = inventoryItem.getStock() - item.getQuantity();
                if (newStock >= 0) {
                    inventoryItem.setStock(newStock);
                    inventoryRepository.save(inventoryItem);
                }
            }
        }

        // Clear the cart
        session.removeAttribute(cartKey);
    }

    public void updateInventoryStock(Inventory inventory) {
        inventoryRepository.save(inventory);
    }
}