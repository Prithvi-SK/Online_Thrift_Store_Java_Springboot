package com.thriftstore.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thriftstore.entity.CartDisplayItem;
import com.thriftstore.entity.CartItem;
import com.thriftstore.entity.Inventory;
import com.thriftstore.entity.User;
import com.thriftstore.repository.InventoryRepository;

import jakarta.servlet.http.HttpSession;

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
        if (item != null && quantity <= item.getStock() && quantity > 0) {
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
        if (cart != null && !cart.isEmpty()) {
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
            session.setAttribute(LAST_CHECKOUT_ATTRIBUTE, checkoutItems);
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
            session.removeAttribute(cartKey); // Clear cart after checkout
        }
    }

    public void updateInventoryStock(Inventory inventory) {
        inventoryRepository.save(inventory);
    }
}