package com.example.StaySearch.StaySearchBackend.JWT;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        userService.registerUser(user);
        String token = jwtUtil.generateToken(user.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("token", token);
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        String token = jwtUtil.generateToken(user.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token);
        return response;
    }
}
