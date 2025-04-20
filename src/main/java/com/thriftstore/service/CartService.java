package com.thriftstore.service;

import com.thriftstore.entity.CartItem;
import com.thriftstore.entity.Inventory;
import com.thriftstore.entity.ReturnItem;
import com.thriftstore.entity.User;
import com.thriftstore.entity.CartDisplayItem;
import com.thriftstore.repository.CartItemRepository;
import com.thriftstore.repository.InventoryRepository;
import com.thriftstore.repository.ReturnItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Added missing import

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ReturnItemRepository returnItemRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    public List<CartDisplayItem> getCartWithDetails(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return new ArrayList<>();
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        return cartItems.stream()
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
        if (user == null) return;
        Inventory item = inventoryRepository.findById(itemId).orElse(null);
        if (item == null || quantity <= 0 || quantity > item.getStock()) return;

        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        Optional<CartItem> existingItem = cartItems.stream()
                .filter(cartItem -> cartItem.getItemId().equals(itemId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            if (newQuantity <= item.getStock()) {
                cartItem.setQuantity(newQuantity);
                cartItemRepository.save(cartItem);
            }
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setItemId(itemId);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    public void checkout(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return;
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) return;

        // Move items to return table
        for (CartItem cartItem : cartItems) {
            ReturnItem returnItem = new ReturnItem();
            returnItem.setUser(user);
            returnItem.setItemId(cartItem.getItemId());
            returnItem.setQuantity(cartItem.getQuantity());
            returnItemRepository.save(returnItem);

            // Decrease inventory stock
            Inventory inventory = inventoryRepository.findById(cartItem.getItemId()).orElse(null);
            if (inventory != null) {
                int newStock = inventory.getStock() - cartItem.getQuantity();
                if (newStock >= 0) {
                    inventory.setStock(newStock);
                    inventoryRepository.save(inventory);
                }
            }
        }

        // Remove items from cart
        cartItemRepository.deleteAll(cartItems);
    }

    public List<CartDisplayItem> getReturnItems(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return new ArrayList<>();
        List<ReturnItem> returnItems = returnItemRepository.findByUser(user);
        return returnItems.stream()
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

    public void confirmReturn(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return;
        List<ReturnItem> returnItems = returnItemRepository.findByUser(user);
        if (returnItems.isEmpty()) return;

        for (ReturnItem returnItem : returnItems) {
            Inventory inventory = inventoryRepository.findById(returnItem.getItemId()).orElse(null);
            if (inventory != null) {
                int newStock = inventory.getStock() + returnItem.getQuantity();
                inventory.setStock(newStock);
                inventoryRepository.save(inventory);
            }
        }

        returnItemRepository.deleteAll(returnItems);
    }

    public void updateInventoryStock(Inventory inventory) {
        inventoryRepository.save(inventory);
    }
}