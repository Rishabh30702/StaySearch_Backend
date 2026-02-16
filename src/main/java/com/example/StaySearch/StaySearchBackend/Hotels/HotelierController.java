package com.example.StaySearch.StaySearchBackend.Hotels;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.StaySearch.StaySearchBackend.CloudinaryConfig.CloudinaryService;
import com.example.StaySearch.StaySearchBackend.Exception.ResourceNotFoundException;
import com.example.StaySearch.StaySearchBackend.JWT.User;
import com.example.StaySearch.StaySearchBackend.JWT.UserRepository;
import com.example.StaySearch.StaySearchBackend.Security.XssSanitizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/hotelier")

public class HotelierController {

    @Autowired
    private HotelierService hotelierService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Hotel_Repository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private Cloudinary cloudinary;


    @GetMapping("/hotels")
    public ResponseEntity<List<Hotel_Entity>> getHotels() {
        return ResponseEntity.ok(hotelierService.getAllHotels());
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<List<Room>> getRooms(@PathVariable Integer hotelId) {
        return ResponseEntity.ok(hotelierService.getRoomsByHotelId(hotelId));
    }

    @PostMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<Room> addRoom(@PathVariable Integer hotelId,
                                        @RequestPart("room") Room room,
                                        @RequestParam("imageUrl") MultipartFile file) throws IOException {
        Room createdRoom = hotelierService.addRoom(hotelId, room, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    @PostMapping("/hotels")
    public ResponseEntity<Hotel_Entity> addHotel(@RequestBody Hotel_Entity hotel) {
        User hotelier = userRepository.findByUsername("hotelier@example.com")
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        hotel.setUser(hotelier); // 🛠️ This sets the user_id foreign key

        Hotel_Entity savedHotel = hotelRepository.save(hotel);
        return ResponseEntity.ok(savedHotel);
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        hotelierService.deleteRoom(roomId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    //to update the content of the rooms
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<?> updateRoomWithFile(
            @PathVariable Long roomId,
            @RequestParam("hotelId") Long hotelId,
            @RequestParam("name") String name,
            @RequestParam("available") int available,
            @RequestParam("total") int total,
            @RequestParam("price") double price,
            @RequestParam(value = "deal", required = false) Boolean deal,
            @RequestParam(value = "description", required = false) String description
            ) {


        // 🛡️ 1. XSS Security Check (Reusing XssSanitizer logic)
        if (isStringMalicious(name) || isStringMalicious(description)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Security Violation: HTML/Scripts are not allowed."));
        }

        // 🛡️ 2. Business Logic Check (Manual range check)
        if (price < 0 || total < 1 || available < 0 || available > total) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Data Violation: Invalid price or room count."));
        }



        try {
            Room updatedRoom = hotelierService.updateRoom(roomId, hotelId, name, available, total, price, deal, description);
            return new ResponseEntity<>(updatedRoom, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    private boolean isStringMalicious(String input) {
        if (input == null || input.isEmpty()) return false;
        return !input.equals(XssSanitizer.sanitize(input));
    }



    //TO update the image of the room
    @PutMapping("/rooms/image/{roomId}")
    public ResponseEntity<String> updateRoomImage(
            @PathVariable Long roomId,
            @RequestParam("imageUrl") MultipartFile newImage) throws IOException {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Delete old image from Cloudinary if it exists
        String oldImageUrl = room.getImageUrl();
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            String publicId = extractPublicIdFromUrl(oldImageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }

        // Upload new image
        Map uploadResult = cloudinary.uploader().upload(newImage.getBytes(), ObjectUtils.emptyMap());
        String newImageUrl = uploadResult.get("url").toString();

        // Update room with new image URL
        room.setImageUrl(newImageUrl);
        roomRepository.save(room);

        return ResponseEntity.ok("Room image updated successfully.");
    }


    private String extractPublicIdFromUrl(String imageUrl) {
        String[] parts = imageUrl.split("/");
        String filenameWithExtension = parts[parts.length - 1]; // gajcesecn8wl4clxu8tk.jpg
        return filenameWithExtension.split("\\.")[0];           // gajcesecn8wl4clxu8tk
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<Room>> getRoomsByHotelId(@PathVariable int hotelId) {
        List<Room> rooms = hotelierService.getRoomsByHotelId(hotelId);
        if (rooms.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // No rooms found
        }
        return new ResponseEntity<>(rooms, HttpStatus.OK); // Return rooms if found
    }

}