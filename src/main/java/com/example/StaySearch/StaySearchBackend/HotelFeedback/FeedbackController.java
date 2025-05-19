package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@CrossOrigin(origins = "*") // Allow frontend requests
public class FeedbackController {

    @Autowired
    private HotelFeedbackService feedbackService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @PostMapping("/feedbacks")
    public ResponseEntity<HotelFeedbackEntities> saveFeedback(
            @RequestBody HotelFeedbackEntities feedback,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // remove "Bearer "
        return ResponseEntity.ok(feedbackService.saveFeedback(feedback, token));
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
}
