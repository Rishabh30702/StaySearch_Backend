package com.example.StaySearch.StaySearchBackend.JWT;


import com.example.StaySearch.StaySearchBackend.Hotels.Hotel_Entity;
import com.example.StaySearch.StaySearchBackend.Hotels.Hotel_Repository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private Hotel_Repository hotelRepository;

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
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUserById(id);
        Map<String, String> response = new HashMap<>();

        if (deleted) {
            response.put("message", "User deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }
    }
    @PostMapping("/me/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> passwordData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User is not authenticated"));
        }

        String username = authentication.getName();
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        boolean isUpdated = userService.updatePassword(username, oldPassword, newPassword);
        if (isUpdated) {
            return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Old password is incorrect."));
        }
    }

    @PostMapping("/wishlist/{hotelId}")
    public ResponseEntity<?> addToWishlist(@PathVariable Integer hotelId, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);

        // ADD THESE LINES BELOW 👇
        System.out.println("Token: " + token);
        System.out.println("Extracted email from token: " + email);
        System.out.println("All users in DB: " + userRepository.findAll());

        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Hotel_Entity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        if (!user.getWishlist().contains(hotel)) {
            user.getWishlist().add(hotel);
            userRepository.save(user);  // This also updates the join table
        }

        return ResponseEntity.ok(Map.of("message", "Hotel added to wishlist."));
    }

    @GetMapping("/wishlist")
    public ResponseEntity<?> getWishlist(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Missing or invalid token"));
        }

        String email = jwtUtil.extractUsername(token);

        Optional<User> userOptional = userRepository.findByUsername(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        User user = userOptional.get();
        List<Hotel_Entity> wishlist = user.getWishlist();

        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/wishlist/{hotelId}")
    public ResponseEntity<?> removeHotelFromWishlist(@PathVariable Integer hotelId, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Missing or invalid token"));
        }

        String email = jwtUtil.extractUsername(token);

        Optional<User> userOptional = userRepository.findByUsername(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }

        Optional<Hotel_Entity> hotelOptional = hotelRepository.findById(hotelId);
        if (hotelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Hotel not found"));
        }

        User user = userOptional.get();
        Hotel_Entity hotel = hotelOptional.get();

        if (user.getWishlist().contains(hotel)) {
            user.getWishlist().remove(hotel);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Hotel removed from wishlist."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Hotel not found in wishlist."));
        }
    }

    //Trigger when the admin approved the hotelier
    @PutMapping("/approve/hotelier/{userId}")
    public ResponseEntity<Map<String, String>> approveHotelier(@PathVariable Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (!"Hotelier".equalsIgnoreCase(user.getRole())) {
                response.put("message", "User is not a hotelier.");
                return ResponseEntity.badRequest().body(response);
            }

            user.setStatus("APPROVED");
            userRepository.save(user);
            userService.sendHotelierApprovalEmail(user);
            response.put("message", "Hotelier approved successfully.");
            return ResponseEntity.ok(response);
        }

        response.put("message", "User not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


    @PostMapping("/pending/{id}")
    public ResponseEntity<Map<String, String>> makeHotelierPending(@PathVariable Long id) {
        userService.updateStatus(id, "PENDING");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hotelier marked as pending successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<Map<String, String>> rejectHotelier(@PathVariable Long id) {
        userService.updateStatus(id, "REJECTED");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hotelier rejected successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending/hoteliers")
    public ResponseEntity<List<User>> getPendingHoteliers() {
        List<User> pendingHoteliers = userRepository.findByRoleAndStatus("ROLE_HOTELIER", "PENDING");
        return ResponseEntity.ok(pendingHoteliers);
    }

    @PostMapping("/login/hotelier")
    public ResponseEntity<Map<String, Object>> loginHotelier(@RequestBody User userRequest) {
        // Step 1: Fetch the user by username or email
        Optional<User> optionalUser = userRepository.findByUsername(userRequest.getUsername());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User not found"));
        }

        User dbUser = optionalUser.get();

        // Step 2: If role is Hotelier, check if status is APPROVED
        if ("Hotelier".equalsIgnoreCase(dbUser.getRole()) &&
                (dbUser.getStatus() == null || !"APPROVED".equalsIgnoreCase(dbUser.getStatus()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Your hotelier account is pending admin approval."));
        }

        // Step 3: Authenticate credentials
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        // Step 4: Generate JWT token
        String token = jwtUtil.generateToken(userRequest.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/hotelier")
    public ResponseEntity<?> registerHotelier(@RequestBody User user) {
        try {
            User registeredUser = userService.registerHotelier(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Hotelier registered successfully. Awaiting admin approval.", "user", registeredUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error registering hotelier", "error", e.getMessage()));
        }
    }


}
