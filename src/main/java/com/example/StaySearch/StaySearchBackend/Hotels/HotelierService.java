package com.example.StaySearch.StaySearchBackend.Hotels;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.StaySearch.StaySearchBackend.CloudinaryConfig.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.StaySearch.StaySearchBackend.Hotels.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class HotelierService {

    @Autowired
    private Hotel_Repository hotelRepo;

    @Autowired
    private RoomRepository roomRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private Cloudinary cloudinary;

    // For now, simulate by loading hotel by static ID
    public List<Hotel_Entity> getAllHotels() {
        return hotelRepo.findAll();
    }

    public List<Room> getRoomsByHotelId(Integer hotelId) {
        return roomRepo.findByHotel_HotelId(hotelId);
    }

    public Room addRoom(Integer hotelId, Room room) {
        Hotel_Entity hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with ID: " + hotelId));

        // Set static image URL
        String staticImageUrl = "https://images.unsplash.com/photo-1501117716987-c8e1ecb210d1?auto=format&fit=crop&w=800&q=80";
        room.setImageUrl(staticImageUrl);

        // Link room to the hotel
        room.setHotel(hotel);
        return roomRepo.save(room);
    }


    public Hotel_Entity createHotel(Hotel_Entity hotel) {
        return hotelRepo.save(hotel);
    }
}
