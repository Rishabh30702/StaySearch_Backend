package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<HotelFeedbackEntities, Long> {
}
