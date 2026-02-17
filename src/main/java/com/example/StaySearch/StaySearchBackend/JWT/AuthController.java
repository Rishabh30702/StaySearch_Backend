package com.example.StaySearch.StaySearchBackend.JWT;


import com.example.StaySearch.StaySearchBackend.Hotels.Hotel_Entity;
import com.example.StaySearch.StaySearchBackend.Hotels.Hotel_Repository;
import com.example.StaySearch.StaySearchBackend.Security.RateLimitingService;
import com.example.StaySearch.StaySearchBackend.Security.XssSanitizer;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RateLimitingService rateLimitingService;

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
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {

        // Create a clean User entity (Prevents Mass Assignment)
        User user = new User();
        user.setUsername(sanitize(request.getUsername()));
        user.setPassword(request.getPassword()); // Service must hash this!


        // FORCE ROLE TO 'USER' - Even if they try to send 'ADMIN' in JSON
        user.setRole("USER");

        userService.registerUser(user);

        String token = jwtUtil.generateToken(user.getUsername());

        Map<String, Object> response = new HashMap<>();

        response.put("token", token);

        // Check the flag from the service
        if (userService.isLastMailFailed()) {
            response.put("message", "User registered, but welcome email failed.");
        } else {
            response.put("message", "User registered successfully!");
        }

        return ResponseEntity.ok(response);
    }

    private String sanitize(String input) {
        if (input == null) return null;
        return input.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("'", "&#39;")
                .replaceAll("\"", "&quot;");
    }



    public static class RegisterRequest {
        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "Please provide a valid email address"
        )
        @Size(min = 5, max = 100, message = "Username must be between 5 and 100 characters")
        private String username;

        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
        private String password;




        // Standard Getters and Setters are MANDATORY for Jackson
        public String getUsername() { return username; }

        public String getPassword() { return password; }




    }





    @PostMapping("/normallogin")
    public ResponseEntity<?> normalLogin(@RequestBody User user, HttpServletRequest request) {
        // 🛡️ 1. Rate Limiting Check (Security Audit Requirement)
        String ip = request.getRemoteAddr();
        Bucket bucket = rateLimitingService.resolveBucket(ip);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many login attempts. Please try again in 20 minutes."));
        }

        try {
            // 🛡️ 2. Authentication (Spring Security)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

            // 🛡️ 3. Token Generation
            String token = jwtUtil.generateToken(user.getUsername());

            // 🛡️ 4. Success Response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            // 🛡️ 5. Handle Invalid Credentials
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User userRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(userRequest.getUsername());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Incorrect Username or Password"));
        }

        User dbUser = optionalUser.get();

        // Only allow users with role USER to login here
        if (!"USER".equalsIgnoreCase(dbUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied: unauthorized role"));
        }

        // Authenticate credentials
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(userRequest.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token);
        return ResponseEntity.ok(response);
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
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

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



    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {

        if (username == null || !username.matches("^[a-zA-Z0-9@._\\-]+$")) {
            return ResponseEntity.badRequest().build();
        }

        boolean available = !userService.checkUsernameExists(username);
        return ResponseEntity.ok(Map.of("available", available));
    }


// admin related apis are now protected using req matchers in security config file
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUserById(id);
        Map<String, String> response = new HashMap<>();

        if (deleted) {
            response.put("message", "User deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Invalid username or password.");
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

        try {
           String isUpdated = userService.updatePassword(username, oldPassword, newPassword);
            if ("SUCCESS".equals(isUpdated)) {
                return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", isUpdated));
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }


    @PutMapping("/admin/user/update")
    public ResponseEntity<?> updateUserDetails(@Valid @RequestBody UpdateUserRequest request) {

        // 1. Manual XSS Sanitization (Primary defense for display data)
        String safeUsername = sanitize(request.getUsername());

        try {
            boolean isUpdated = userService.updateUserDetails(
                    safeUsername,
                    request.getNewPassword(),
                    request.getPhone(),
                    request.getRole()
            );

            if (isUpdated) {
                return ResponseEntity.ok(Map.of("message", "User updated successfully."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Invalid username or password."));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Internal error occurred during update."));
        }
    }



    // 2. The Request Class with Specific Role Whitelisting
    public static class UpdateUserRequest {
        @NotBlank(message = "Username is required")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "Username must be a valid email format")
        private String username;

        @Size(min = 8, max = 100)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password does not meet complexity requirements"
        )
        private String newPassword;

        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone format")
        private String phone;

        // ROLE RESTRICTION: Only Admin, admin, USER, or Hotelier allowed.
        // This stops SQL Keywords (SELECT, EXEC, DECLARE) because they aren't in this list.
        @Pattern(regexp = "(?i)^(ADMIN|USER|HOTELIER)$",
                message = "Invalid Role. Only ADMIN, USER, or Hotelier are accepted.")
        private String role;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
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
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Invalid username or password"));
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Invalid username or password"));
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

            // 2. Attempt to send email with safety catch
            try {
                userService.sendHotelierApprovalEmail(user);
                response.put("message", "Hotelier approved successfully and notification email sent.");
            } catch (Exception e) {

                // Inform the Admin that the approval worked, but the mail failed
                response.put("message", "Hotelier approved successfully, but welcome email could not be sent.");
            }

            return ResponseEntity.ok(response);



        }

        response.put("message", "Invalid username or passowrd.");
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
    public ResponseEntity<Map<String, String>> rejectHotelier(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        String remark = request.get("remark"); // Get remark from JSON body
        userService.updateStatusAndRemark(id, "REJECTED", remark);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Hotelier rejected successfully.");
        response.put("remark", remark);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending/hoteliers")
    public ResponseEntity<List<User>> getPendingHoteliers() {
        List<User> pendingHoteliers = userRepository.findByRoleAndStatus("ROLE_HOTELIER", "PENDING");
        return ResponseEntity.ok(pendingHoteliers);
    }

//    @PostMapping("/login/hotelier")
//    public ResponseEntity<Map<String, Object>> loginHotelier(@RequestBody User userRequest) {
//        // Step 1: Fetch the user by username or email
//        Optional<User> optionalUser = userRepository.findByUsername(userRequest.getUsername());
//
//        if (optionalUser.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(Map.of("message", "User not found"));
//        }
//
//        User dbUser = optionalUser.get();
//
//        // Step 2: If role is Hotelier, check if status is APPROVED
//        if ("Hotelier".equalsIgnoreCase(dbUser.getRole()) &&
//                (dbUser.getStatus() == null || !"APPROVED".equalsIgnoreCase(dbUser.getStatus()))) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("message", "Your hotelier account is pending admin approval."));
//        }
//
//        // Step 3: Authenticate credentials
//        try {
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword()));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(Map.of("message", "Invalid credentials"));
//        }
//
//        // Step 4: Generate JWT token
//        String token = jwtUtil.generateToken(userRequest.getUsername());
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "Login successful");
//        response.put("token", token);
//        return ResponseEntity.ok(response);
//    }


   /* @PostMapping("/login/hotelier")
    public ResponseEntity<Map<String, Object>> loginHotelier(@RequestBody User userRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(userRequest.getUsername());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User Invalid username or password"));
        }

        User dbUser = optionalUser.get();

        // Explicitly reject if role is not "Hotelier"
        if (!"Hotelier".equalsIgnoreCase(dbUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied: not a hotelier account"));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(userRequest.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
*/

    @PostMapping("/register/hotelier")
    public ResponseEntity<?> registerHotelier(@RequestBody User user) {

        try {
            User registeredUser = userService.registerHotelier(user);

            // If we reached here, DB save was successful.
            String message = registeredUser.isMailFailed()
                    ? "Registration success, but email failed."
                    : "Registration successful!";

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", message, "user", registeredUser));

        } catch (Exception e) {
            // This now catches DB errors, connection errors, and null pointers
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Database Error: Registration failed", "error", e.getMessage()));
        }
    }


//
   /* @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody User userRequest) {
        try {
            // Check if username already exists
            Optional<User> existingUser = userRepository.findByUsername(userRequest.getUsername());
            if (existingUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("message", "Username already exists"));
            }

            // Set admin-specific fields
            userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));
            userRequest.setRole("ADMIN"); // or "ROLE_ADMIN" if using Spring Security roles
            userRequest.setStatus("APPROVED");

            User savedAdmin = userRepository.save(userRequest);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Admin registered successfully", "user", savedAdmin));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error registering admin", "error", e.getMessage()));
        }
    }
*/


    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody User userRequest, HttpServletRequest request) {
        // 🛡️ 1. Rate Limiting Check (Stricter for Admin)
        String ip = request.getRemoteAddr();
        Bucket bucket = rateLimitingService.resolveAdminBucket(ip); // resolveAdminBucket(ip)

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many admin login attempts. Try again Later."));
        }

        // 🛡️ 2. Fetch User & Check Role BEFORE Authenticating
        // This saves CPU cycles if the user isn't even an admin
        Optional<User> optionalUser = userRepository.findByUsername(userRequest.getUsername());

        if (optionalUser.isEmpty() || !"ADMIN".equalsIgnoreCase(optionalUser.get().getRole())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        try {
            // 🛡️ 3. Authenticate
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword()));

            // 🛡️ 4. Generate Admin Token
            String token = jwtUtil.generateToken(userRequest.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin login successful");
            response.put("token", token);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }




    @GetMapping("/me/status")
    public ResponseEntity<Map<String, String>> getCurrentUserStatus() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User is not authenticated"));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        Map<String, String> response = new HashMap<>();
        response.put("status", user.getStatus());

        // If rejected, include the remark
        if ("REJECTED".equalsIgnoreCase(user.getStatus())) {
            response.put("remark", user.getRejectionRemark());
        }

        return ResponseEntity.ok(response); // ✅ return the response with remark
    }


}
