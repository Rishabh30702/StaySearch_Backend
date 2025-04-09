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

    public Room updateRoom(Long roomId, Room updatedRoom, MultipartFile imageFile) {
        Room existingRoom = roomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        // Update fields from form-data
        existingRoom.setName(updatedRoom.getName());
        existingRoom.setPrice(updatedRoom.getPrice());
        existingRoom.setDescription(updatedRoom.getDescription());

        // Optional business fields (you mentioned in the payload)
        existingRoom.setAvailable(updatedRoom.getAvailable());
        existingRoom.setTotal(updatedRoom.getTotal());
        existingRoom.setDeal(updatedRoom.isDeal());

        // Upload image if new one is provided
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
                String imageUrl = uploadResult.get("url").toString();
                existingRoom.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed", e);
            }
        }

        return roomRepo.save(existingRoom);
    }

}
