package com.example.StaySearch.StaySearchBackend.JWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import javax.naming.*;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
