package com.thriftstore.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thriftstore.entity.CartDisplayItem;
import com.thriftstore.entity.CartItem;
import com.thriftstore.entity.Inventory;
import com.thriftstore.entity.ReturnItem;
import com.thriftstore.entity.User;
import com.thriftstore.repository.CartItemRepository;
import com.thriftstore.repository.InventoryRepository;
import com.thriftstore.repository.ReturnItemRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class CartService {

    private static CartService instance;

    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ReturnItemRepository returnItemRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    private InventoryUpdateStrategy inventoryUpdateStrategy;

    // Private constructor for Singleton
    private CartService() {
        this.inventoryUpdateStrategy = new DefaultInventoryUpdateStrategy();
    }

    // Singleton instance method
    public static synchronized CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }

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
                notifyObservers("Cart updated for user: " + user.getEmail());
            }
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setItemId(itemId);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            notifyObservers("Item added to cart for user: " + user.getEmail());
        }
    }

    public void checkout(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return;
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) return;

        for (CartItem cartItem : cartItems) {
            ReturnItem returnItem = new ReturnItem();
            returnItem.setUser(user);
            returnItem.setItemId(cartItem.getItemId());
            returnItem.setQuantity(cartItem.getQuantity());
            returnItemRepository.save(returnItem);
            inventoryUpdateStrategy.updateStock(cartItem.getItemId(), -cartItem.getQuantity());
        }

        cartItemRepository.deleteAll(cartItems);
        notifyObservers("Checkout completed for user: " + user.getEmail());
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
            inventoryUpdateStrategy.updateStock(returnItem.getItemId(), returnItem.getQuantity());
        }

        returnItemRepository.deleteAll(returnItems);
        notifyObservers("Return completed for user: " + user.getEmail());
    }

    // Observer pattern implementation
    private List<CartObserver> observers = new ArrayList<>();

    public void addObserver(CartObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(CartObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String message) {
        for (CartObserver observer : observers) {
            observer.update(message);
        }
    }

    public void setInventoryUpdateStrategy(InventoryUpdateStrategy strategy) {
        this.inventoryUpdateStrategy = strategy;
    }

    public void updateInventoryStock(Inventory inventory) {
        inventoryRepository.save(inventory);
    }
}

// Strategy Interface and Implementation
interface InventoryUpdateStrategy {
    void updateStock(Long itemId, int quantityChange);
}

class DefaultInventoryUpdateStrategy implements InventoryUpdateStrategy {
    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public void updateStock(Long itemId, int quantityChange) {
        Inventory inventory = inventoryRepository.findById(itemId).orElse(null);
        if (inventory != null) {
            int newStock = inventory.getStock() + quantityChange;
            if (newStock >= 0) {
                inventory.setStock(newStock);
                inventoryRepository.save(inventory);
            }
        }
    }
}