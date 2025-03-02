package com.example.StaySearch.StaySearchBackend.Hotels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface Hotel_Repository extends JpaRepository<Hotel_Entity,Integer> {
    @Query("SELECT h FROM Hotel_Entity h WHERE h.hotel_name = :hotel_name")
    Optional<Hotel_Entity> findByHotelName(@Param("hotel_name") String hotel_name);
}
