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


    public List<HotelFeedbackEntities> getFeedbackByLoggedInUser(String token) {
        String username = jwtUtil.extractUsername(token);
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.map(feedbackRepository::findByUser).orElse(List.of());
    }

    public HotelFeedbackEntities updateMyFeedback(Long feedbackId, HotelFeedbackEntities updatedFeedback, String token) {
        String username = jwtUtil.extractUsername(token);
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User currentUser = userOpt.get();
            Optional<HotelFeedbackEntities> existingOpt = feedbackRepository.findById(feedbackId);

            if (existingOpt.isPresent()) {
                HotelFeedbackEntities existing = existingOpt.get();

                if (existing.getUser().getId().equals(currentUser.getId())) {
                    // Update allowed
                    existing.setRating(updatedFeedback.getRating());
                    existing.setDescription(updatedFeedback.getDescription());
                    existing.setLikedAmenities(updatedFeedback.getLikedAmenities());
                    return feedbackRepository.save(existing);
                }
            }
        }
        return null;
    }

    public boolean deleteMyFeedback(Long id, String token) {
        String username = jwtUtil.extractUsername(token);
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            Optional<HotelFeedbackEntities> feedbackOpt = feedbackRepository.findById(id);
            if (feedbackOpt.isPresent() && feedbackOpt.get().getUser().getId().equals(userOpt.get().getId())) {
                feedbackRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }

}

