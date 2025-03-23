package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/feedbacks")
@CrossOrigin(origins = "*") // Allow frontend requests
public class FeedbackController {

    @Autowired
    private HotelFeedbackService feedbackService;

    @PostMapping("/submit")
    public HotelFeedbackEntities submitFeedback(@RequestBody HotelFeedbackEntities feedback) {
        return feedbackService.saveFeedback(feedback);
    }

    @GetMapping("/getAllFeedbacks")
    public List<HotelFeedbackEntities> getAllFeedbacks() {
        return feedbackService.getAllFeedbacks();
    }
}
