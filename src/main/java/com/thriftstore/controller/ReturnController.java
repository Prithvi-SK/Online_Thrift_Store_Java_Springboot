package com.thriftstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class ReturnController {

    @GetMapping("/return")
    public String showReturn(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/";
        }
        return "return";
    }
}