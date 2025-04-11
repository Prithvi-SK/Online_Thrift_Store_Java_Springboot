// package com.thriftstore.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// import com.thriftstore.entity.User;
// import com.thriftstore.service.UserService;

// @Controller
// public class UserController {

//     @Autowired
//     private UserService userService;

//     @GetMapping("/")
//     public String loginPage() {
//         return "login";
//     }

//     @GetMapping("/signup")
//     public String signupPage() {
//         return "signup";
//     }

//     @PostMapping("/signup")
//     public String signup(
//             @RequestParam String username,
//             @RequestParam String email,
//             @RequestParam String password,
//             @RequestParam String role,
//             Model model) {
//         try {
//             userService.signup(username, email, password, role);
//             return "redirect:/";
//         } catch (RuntimeException e) {
//             model.addAttribute("error", e.getMessage());
//             return "signup";
//         }
//     }

//     @PostMapping("/login")
//     public String login(
//             @RequestParam String email,
//             @RequestParam String password,
//             Model model) {
//         User user = userService.login(email, password);
//         if (user != null) {
//             // For now, redirect to a placeholder dashboard
//             return "redirect:/dashboard";
//         } else {
//             model.addAttribute("error", "Invalid email or password");
//             return "login";
//         }
//     }

//     @GetMapping("/dashboard")
//     public String dashboard() {
//         return "dashboard"; // Placeholder until next instructions
//     }
// }





package com.thriftstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.thriftstore.entity.User;
import com.thriftstore.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            Model model) {
        try {
            userService.signup(username, email, password, role);
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "signup";
        }
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            Model model,
            HttpSession session) {
        User user = userService.login(email, password);
        if (user != null) {
            session.setAttribute("user", user); // Store user in session
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/inventory";
            } else {
                return "redirect:/dashboard";
            }
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}