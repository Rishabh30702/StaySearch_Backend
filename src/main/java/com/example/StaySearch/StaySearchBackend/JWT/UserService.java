package com.example.StaySearch.StaySearchBackend.JWT;

import com.example.StaySearch.StaySearchBackend.Security.XssSanitizer;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j; // Add this import



import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; // ✅ Inject EmailService

    private boolean lastMailFailed = false;
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPasswordLastChangedAt(System.currentTimeMillis());
        User savedUser = userRepository.save(user);

        try {
            System.out.println("Sending welcome email to User: " + savedUser.getUsername()  );
            sendWelcomeEmail(savedUser.getUsername());
            lastMailFailed = false;
        } catch (Exception e) {
            System.err.println("Email Error: " + e.getMessage());
            lastMailFailed = true; // Set the flag
        }// ✅ Sends email after registration
        return savedUser;
    }

    public boolean isLastMailFailed() {
        return lastMailFailed;
    }

    @Transactional // Ensures database integrity
    public User registerHotelier(User user) {

        // 2. Tightened Validation
        validatePassword(user.getPassword());
        // 1. Prepare User Data
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("Hotelier");
        user.setStatus("PENDING");

        // 2. Save User (If this fails, an exception is thrown and the method stops)
        User savedUser = userRepository.save(user);

        // 3. Attempt Email (Failure here does NOT stop the registration)
        try {
            sendHotelierRegistrationEmail(savedUser);
            savedUser.setMailFailed(false);
        } catch (Exception e) {
            // Log the error for Prod debugging
            log.error("CRITICAL: User saved (ID: {}), but email failed: {}",
                    savedUser.getId(), e.getMessage());

            savedUser.setMailFailed(true);
        }

        return savedUser;
    }


    public void validatePassword(String password) {
        // 1. Strict Regex: 8-32 chars, must contain Upper, Lower, Digit, and Special.
        // No whitespace allowed.
        String strictRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,32}$";

        if (password == null) throw new IllegalArgumentException("Password cannot be null.");

        if (!password.matches(strictRegex)) {
            throw new IllegalArgumentException("Password must be 8-32 chars, with upper, lower, digit, and special char (@$!%*?&).");
        }

        // 2. Prevent Repeated Characters (e.g., "aaaaa", "11111")
        if (Pattern.compile("(.)\\1{3,}").matcher(password).find()) {
            throw new IllegalArgumentException("Password contains too many repeating characters.");
        }


    }



    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    private void sendWelcomeEmail(String userEmail) {
        String subject = "Welcome to StaySearch!";
        String content = "<div style='font-family: Arial, sans-serif; text-align: center; background-color: #f4f4f4; padding: 20px;'>"
                + "<div style='max-width: 600px; margin: auto; background: white; padding: 20px; border-radius: 10px;'>"
                + "<h1 style='color: #4CAF50;'>Welcome to StaySearch!</h1>"
                + "<p style='font-size: 16px; color: #333;'>Thank you for registering. We are excited to have you with us.</p>"
                + "<p style='font-size: 16px; color: #333;'>Start exploring now by clicking the button below!</p>"
                + "<a href='https://staging.valliento.tech' style='display: inline-block; padding: 12px 25px; font-size: 18px; color: white; background-color: #4CAF50; text-decoration: none; border-radius: 5px;'>Visit StaySearch</a>"
                + "<p style='margin-top: 20px; color: #777;'>If you have any questions, feel free to contact us.</p>"
                + "</div>"
                + "</div>";

        emailService.sendEmail(userEmail, subject, content);
    }
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
        }

        return null;
    }

    //Save fullname and phonenmber of the user
    public void saveDetailsForCurrentUser(String fullname, String phonenumber) {
        String currentEmail = getCurrentUserEmail(); // This gets the username/email of the current logged-in user
        if (currentEmail != null) {
            Optional<User> optionalUser = userRepository.findByUsername(currentEmail);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setFullname(fullname);
                user.setPhonenumber(phonenumber);
                userRepository.save(user);
            }
        }
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public boolean deleteUserById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    public String updatePassword(String email, String oldPassword, String newPassword) {
        System.out.println("Searching for user with email: [" + email + "]");
        User user = userRepository.findByUsername(email).orElse(null);

        if (user == null) {
            return "Invalid username or password.";
        }

        // 1. Check old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "Old password is incorrect.";


        }

        // 2. Prevent old password reuse
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException(
                    "New password cannot be the same as the old password"
            );
        }

        // 3. Enforce password complexity
        validatePasswordStrength(newPassword);

        user.setPasswordLastChangedAt(System.currentTimeMillis());
        // 4. Save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "SUCCESS";
    }



    private static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    private void validatePasswordStrength(String password) {
        if (!password.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException(
                    "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character"
            );
        }
    }



    public void sendHotelierRegistrationEmail(User user) {
        String to = user.getUsername(); // assuming it's an email
        String subject = "Thanks for Registering – StaySearch";

        String body = "<div style='font-family: Arial, sans-serif; color: #333;'>"
                + "<h2 style='color: #761461;'>Welcome to StaySearch, " + user.getFullname() + "!</h2>"
                + "<p>Thank you for registering as a <strong>hotelier</strong> on our platform. 🏨</p>"
                + "<p>Your request is currently under review by our admin team.</p>"
                + "<p><strong>Status:</strong> <span style='color: orange;'>PENDING</span></p>"
                + "<hr>"
                + "<p style='font-size: 14px;'>We'll notify you once your account is approved.</p>"
                + "<br>"
                + "<p>Warm regards,</p>"
                + "<p><strong>StaySearch Team</strong></p>"
                + "<img src='https://cdn-icons-png.flaticon.com/512/235/235861.png' width='80' alt='StaySearch Logo'>"
                + "</div>";

        emailService.sendEmail(to, subject, body);
    }


    public void sendHotelierApprovalEmail(User user) {
        String to = user.getUsername();
        String subject = "You’re Approved – Start Listing Hotels on StaySearch!";

        String body = "<div style='font-family: Arial, sans-serif; color: #333;'>"
                + "<h2 style='color: #28a745;'>🎉 Congratulations, " + user.getFullname() + "!</h2>"
                + "<p>Your hotelier registration on <strong>StaySearch</strong> has been <span style='color: #28a745; font-weight: bold;'>APPROVED</span>.</p>"
                + "<p>You can now log in and start listing your amazing hotels on our platform.</p>"
                + "<p style='margin-top: 20px;'>👉 "
                + "<a href='https://staging.valliento.tech' "
                + "style='background-color: #761461; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>"
                + "Access Your Dashboard"
                + "</a></p>"
                + "<hr style='margin: 30px 0;'>"
                + "<p>We’re excited to have you on board and can’t wait to see your listings live! 🚀</p>"
                + "<p>Best regards,<br><strong>StaySearch Team</strong></p>"
                + "<img src='https://cdn-icons-png.flaticon.com/512/235/235861.png' width='80' alt='StaySearch Logo'>"
                + "</div>";

        emailService.sendEmail(to, subject, body);
    }
    public ResponseEntity<?> updateStatus(Long id, String status) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            User hotelier = optional.get();
            hotelier.setStatus(status);
            userRepository.save(hotelier);
            return ResponseEntity.ok().body("Status updated to " + status);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid username name or password");
        }
    }

    public ResponseEntity<?> updateStatusAndRemark(Long id, String status, String remark) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            User hotelier = optional.get();
            hotelier.setStatus(status);
            hotelier.setRejectionRemark(remark);  // save remark
            userRepository.save(hotelier);

            return ResponseEntity.ok().body("Status updated to " + status + " with remark: " + remark);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid username or password");
        }
    }


    public boolean resetPassword(String username, String newPassword) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);  // Safe: only password is changed
            return true;
        }
        return false;
    }

    public boolean updateUserDetails(String username, String newPassword, String phone, String role) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Update password if provided
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(newPassword));
            }

            // Update phone if provided
            if (phone != null && !phone.trim().isEmpty()) {
                user.setPhonenumber(phone);
            }

            // Use exact casing that matches the DB: "ADMIN", "Hotelier", "USER"
            if (role != null && !role.trim().isEmpty()) {
                List<String> validRoles = List.of("ADMIN", "Hotelier", "USER");
                if (!validRoles.contains(role)) {
                    throw new IllegalArgumentException("Invalid role: " + role);
                }
                user.setRole(role);
            }

            userRepository.save(user);
            return true;
        }

        return false;
    }



    public boolean checkUsernameExists(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }

        String cleanUsername = username.replaceAll("[<>]", "").trim().toLowerCase();

        // 🛡️ Normalization: Ensure case-insensitive matching
        return userRepository.existsByUsernameIgnoreCase(cleanUsername);
    }


}
