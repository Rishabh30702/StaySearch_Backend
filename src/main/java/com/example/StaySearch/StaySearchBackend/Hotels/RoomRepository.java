package com.example.StaySearch.StaySearchBackend.Hotels;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotel_HotelId(Integer hotelId);

    List<Room> findByHotelUserUsername(String username);

    List<Room> findByHotel_HotelId(int hotelId);

}
