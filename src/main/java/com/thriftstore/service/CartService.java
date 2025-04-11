package com.thriftstore.service;

import com.thriftstore.entity.CartItem;
import com.thriftstore.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private static final String CART_SESSION_ATTRIBUTE = "cart";

    public List<CartItem> getCart(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_ATTRIBUTE);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_ATTRIBUTE, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Long itemId, int quantity) {
        List<CartItem> cart = getCart(session);
        // Logic to add item (simplified, to be expanded in Case 3)
        CartItem cartItem = new CartItem();
        cartItem.setItemId(itemId);
        cartItem.setQuantity(quantity);
        cart.add(cartItem);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_ATTRIBUTE);
    }
}