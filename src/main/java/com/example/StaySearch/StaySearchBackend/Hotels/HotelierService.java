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

    public Room addRoom(Integer hotelId, Room room, MultipartFile file) throws IOException{
        Hotel_Entity hotel = hotelRepo.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with ID: " + hotelId));

        try{
        // Set static image URL
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String imageUrl = uploadResult.get("url").toString();  // Get the URL of the uploaded image;
        room.setImageUrl(imageUrl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
        // Link room to the hotel
        room.setHotel(hotel);
        return roomRepo.save(room);
    }


    public Hotel_Entity createHotel(Hotel_Entity hotel) {
        return hotelRepo.save(hotel);
    }

    public void deleteRoom(Long roomId) {
        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));
        roomRepo.delete(room);
    }

    public Room updateRoom(Long roomId, Long hotelId, String name, int available,
                           int total, Double price, Boolean deal,
                           String description) {

        // Find the existing room in the repository
        Room existingRoom = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        // Update room fields with the new data
        existingRoom.setName(name);
        existingRoom.setAvailable(available); // Assuming available is a string in frontend
        existingRoom.setTotal(total);
        existingRoom.setPrice(price);
        existingRoom.setDeal(deal != null ? deal : false); // Default to false if deal is not provided
        existingRoom.setDescription(description != null ? description : ""); // Default to empty string if description is null

        // Save the updated room to the database
        return roomRepo.save(existingRoom);
    }


    // Method to get rooms by hotel ID
    public List<Room> getRoomsByHotelId(int hotelId) {
        return roomRepo.findByHotel_HotelId(hotelId); // Adjust based on your database query
    }

}
