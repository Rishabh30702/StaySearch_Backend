package com.example.StaySearch.StaySearchBackend.Hotels;

import com.example.StaySearch.StaySearchBackend.JWT.User;
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

    List<Hotel_Entity> findByUserUsername(String username);

    Optional<Hotel_Entity> findByUserAndName(User user, String name);

}
