package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import com.example.StaySearch.StaySearchBackend.JWT.JwtUtil;
import com.example.StaySearch.StaySearchBackend.JWT.User;
import com.example.StaySearch.StaySearchBackend.JWT.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class HotelFeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public HotelFeedbackEntities saveFeedback(HotelFeedbackEntities feedback, String token) {
        String username = jwtUtil.extractUsername(token);
        Optional<User> userOpt = userRepository.findByUsername(username); // Use findByUsername or findByEmail
        if (userOpt.isPresent()) {
            feedback.setUser(userOpt.get()); // ✅ associate logged-in user
        }
        return feedbackRepository.save(feedback);
    }

    public List<HotelFeedbackEntities> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    public List<HotelFeedbackEntities> getFeedbackForHotel(Integer hotelId) {
        return feedbackRepository.findByHotel_HotelId(hotelId);
    }

    public long getReviewCountForHotel(Integer hotelId) {
        return feedbackRepository.countByHotel_HotelId(hotelId);
    }
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}

