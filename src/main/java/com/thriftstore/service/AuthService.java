package com.thriftstore.service;

import com.thriftstore.entity.User;

public interface AuthService {
    User signup(String username, String email, String password, String role);
    User login(String email, String password);
}