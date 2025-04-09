package com.example.StaySearch.StaySearchBackend.Hotels;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface Hotel_Repository extends JpaRepository<Hotel_Entity,Integer> {
    @Query("SELECT h FROM Hotel_Entity h WHERE h.name = :name")
    Optional<Hotel_Entity> findByHotelName(@Param("name") String name);

    @Repository
    public interface HotelRepository extends JpaRepository<Hotel_Entity, Integer> {
        List<Hotel_Entity> findByName(String name); // Add more as needed
    }
}
