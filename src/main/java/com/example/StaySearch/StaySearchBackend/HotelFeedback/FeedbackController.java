package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@CrossOrigin(origins = "*") // Allow frontend requests
public class FeedbackController {

    @Autowired
    private HotelFeedbackService feedbackService;

    @PostMapping("/feedbacks")
    public ResponseEntity<HotelFeedbackEntities> saveFeedback(
            @RequestBody HotelFeedbackEntities feedback,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // remove "Bearer "
        return ResponseEntity.ok(feedbackService.saveFeedback(feedback, token));
    }

    @GetMapping("/getAllFeedbacks")
    public List<HotelFeedbackEntities> getAllFeedbacks() {
        return feedbackService.getAllFeedbacks();
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
}
