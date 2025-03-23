package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HotelFeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public HotelFeedbackEntities saveFeedback(HotelFeedbackEntities feedback) {
        return feedbackRepository.save(feedback);
    }

    public List<HotelFeedbackEntities> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }
}

