package com.example.StaySearch.StaySearchBackend.Hotels;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.StaySearch.StaySearchBackend.CloudinaryConfig.CloudinaryService;
import com.example.StaySearch.StaySearchBackend.JWT.User;
import com.example.StaySearch.StaySearchBackend.JWT.UserRepository;
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
@CrossOrigin(origins = "*")
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
                .orElseThrow(() -> new RuntimeException("Hotelier not found"));

        hotel.setUser(hotelier); // 🛠️ This sets the user_id foreign key

        Hotel_Entity savedHotel = hotelRepository.save(hotel);
        return ResponseEntity.ok(savedHotel);
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        hotelierService.deleteRoom(roomId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<?> updateRoom(
            @PathVariable Long roomId,
            @RequestPart("room") Room updatedRoom,
            @RequestPart(value = "imageUrl", required = false) MultipartFile imageFile) {

        try {
            Room room = hotelierService.updateRoom(roomId, updatedRoom, imageFile);
            return new ResponseEntity<>(room, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
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