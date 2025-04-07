package com.example.StaySearch.StaySearchBackend.JWT;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private UserRepository userRepository;

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
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            System.out.println("No authentication found in SecurityContext.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authentication found");
        }

        if (!authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            System.out.println("User not authenticated or anonymous: " + authentication.getPrincipal());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        String username = authentication.getName();
        System.out.println("Authenticated user: " + username);

        // Assuming username is unique and corresponds to the user's email
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getUsername());
        response.put("fullName", user.getFullname());
        response.put("phoneNumber", user.getPhonenumber());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/update")
    public ResponseEntity<Map<String, String>> updateUserProfile(@RequestBody User request) {
        userService.saveDetailsForCurrentUser(request.getFullname(), request.getPhonenumber());

        Map<String, String> response = new HashMap<>();
        response.put("message", "User profile updated successfully.");

        return ResponseEntity.ok(response);
    }
    @GetMapping("/allUsers")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUserById(id);
        if (deleted) {
            return ResponseEntity.ok("User deleted successfully.");
        } else {
            return ResponseEntity.status(404).body("User not found.");
        }
    }
}
