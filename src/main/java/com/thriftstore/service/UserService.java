package com.thriftstore.service;

import com.thriftstore.entity.User;
import com.thriftstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User signup(String username, String email, String password, String role) {
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password); // Should be hashed in production
        newUser.setRole(role);
        return userRepository.save(newUser);
    }

    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public void updateUser(User user) {
        // Only update username and password, keep email unchanged
        User existingUser = userRepository.findById(user.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setUsername(user.getUsername());
        existingUser.setPassword(user.getPassword()); // Should be hashed in production
        userRepository.save(existingUser);
    }
}