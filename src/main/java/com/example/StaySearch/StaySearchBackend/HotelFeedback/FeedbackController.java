package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import com.example.StaySearch.StaySearchBackend.Hotels.Hotel_Entity;
import com.example.StaySearch.StaySearchBackend.Hotels.Hotel_Repository;
import com.example.StaySearch.StaySearchBackend.JWT.JwtUtil;
import com.example.StaySearch.StaySearchBackend.JWT.User;
import com.example.StaySearch.StaySearchBackend.JWT.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedbacks")
// Allow frontend requests
public class FeedbackController {

    @Autowired
    private HotelFeedbackService feedbackService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Hotel_Repository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/feedbacks")
    public ResponseEntity<HotelFeedbackEntities> saveFeedback(
            @RequestBody HotelFeedbackEntities feedback,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // remove "Bearer "
        return ResponseEntity.ok(feedbackService.saveFeedback(feedback, token));
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveFeedback(
            @RequestBody HotelFeedbackDTO dto,
            @RequestHeader("Authorization") String authHeader) {

        // Remove "Bearer " prefix
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        HotelFeedbackEntities feedback = new HotelFeedbackEntities();
        feedback.setHotelName(dto.getHotelName());
        feedback.setLikedAmenities(dto.getLikedAmenities());
        feedback.setRating(dto.getRating());
        feedback.setDescription(dto.getDescription());

        if (dto.getHotelId() != null) {
            Optional<Hotel_Entity> hotelOpt = hotelRepository.findById(dto.getHotelId());
            hotelOpt.ifPresent(feedback::setHotel);
        }

        HotelFeedbackEntities saved = feedbackService.saveFeedback(feedback, token);
        return ResponseEntity.ok(saved);
    }




    @PutMapping("/feedbacks/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveFeedback(@PathVariable Long id) {
        return feedbackRepository.findById(id)
                .map(feedback -> {
                    feedback.setStatus(FeedbackStatus.APPROVED);
                    feedbackRepository.save(feedback);
                    return ResponseEntity.ok("Feedback approved.");
                }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/getAllFeedbacks")
    public List<HotelFeedbackEntities> getAllFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }

    @GetMapping("/feedbacks/public")
    public ResponseEntity<List<HotelFeedbackEntities>> getApprovedFeedbacks() {
        return ResponseEntity.ok(feedbackRepository.findByStatus(FeedbackStatus.APPROVED));
    }
    @GetMapping("/hotel/{hotelId}")
    public List<HotelFeedbackEntities> getFeedbackForHotel(@PathVariable Integer hotelId) {
        return feedbackService.getFeedbackForHotel(hotelId);
    }

    @GetMapping("/hotel/{hotelId}/count")
    public long getReviewCount(@PathVariable Integer hotelId) {
        return feedbackService.getReviewCountForHotel(hotelId);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok("Feedback deleted successfully");
    }

    @GetMapping("/my-feedbacks")
    public ResponseEntity<List<HotelFeedbackEntities>> getMyFeedbacks(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // remove Bearer
        return ResponseEntity.ok(feedbackService.getFeedbackByLoggedInUser(token));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateFeedback(
            @PathVariable Long id,
            @RequestBody HotelFeedbackEntities updatedFeedback,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        HotelFeedbackEntities result = feedbackService.updateMyFeedback(id, updatedFeedback, token);
        if (result != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(403).body("Unauthorized to update this feedback");
        }
    }

    @DeleteMapping("/my-feedback/delete/{id}")
    public ResponseEntity<String> deleteMyFeedback(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        boolean deleted = feedbackService.deleteMyFeedback(id, token);
        if (deleted) {
            return ResponseEntity.ok("Feedback deleted successfully");
        } else {
            return ResponseEntity.status(403).body("Unauthorized to delete this feedback");
        }
    }

    @GetMapping("/hotelier/my-feedbacks")
    public ResponseEntity<List<HotelFeedbackEntities>> getFeedbacksForHotelierPortal(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            List<HotelFeedbackEntities> feedbacks =
                    feedbackService.getFeedbacksForHotelier(userOpt.get().getId().intValue());
            return ResponseEntity.ok(feedbacks);
        } else {
            return ResponseEntity.status(401).build();
        }
    }


}
