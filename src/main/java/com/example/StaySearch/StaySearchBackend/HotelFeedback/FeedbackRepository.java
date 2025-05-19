package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import com.example.StaySearch.StaySearchBackend.JWT.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<HotelFeedbackEntities, Long> {
    List<HotelFeedbackEntities> findByHotel_HotelId(Integer hotelId);
    long countByHotel_HotelId(Integer hotelId);

    List<HotelFeedbackEntities> findByUser(User user);

    List<HotelFeedbackEntities> findByStatus(FeedbackStatus status);


}
