package com.example.StaySearch.StaySearchBackend.JWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; // ✅ Inject EmailService


    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        sendWelcomeEmail(savedUser.getUsername()); // ✅ Sends email after registration
        return savedUser;
    }

    public User registerHotelier(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("Hotelier");
        user.setStatus("PENDING");
        User savedUser = userRepository.save(user);

        sendHotelierRegistrationEmail(savedUser);
        return savedUser;
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
    public boolean updatePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(email).orElse(null);
        if (user != null && passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hotelier not found");
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


}
