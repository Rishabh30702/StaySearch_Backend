package com.example.StaySearch.StaySearchBackend.Hotels;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.StaySearch.StaySearchBackend.JWT.User;
import com.example.StaySearch.StaySearchBackend.JWT.UserRepository;
import com.example.StaySearch.StaySearchBackend.JWT.UserService;
import com.example.StaySearch.StaySearchBackend.Security.XssSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("v1")

public class Hotel_Controller {

    @Autowired
    private Hotel_Service hotelService;
    @Autowired
    private Hotel_Repository hotelRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ObjectMapper objectMapper; // Inject the configured ObjectMapper


    //Function to fetch all the hotels
    @GetMapping("/hotels")
    private ResponseEntity<?> getAllHotels() {
        return new ResponseEntity<>(hotelService.getAllHotels(), HttpStatus.OK);
    }

    //To save the hotels
    @PostMapping("/saveHotels")
    public ResponseEntity<?> saveHotel(@RequestBody Hotel_Entity hotelEntity) {
//        try {
//            Hotel_Entity savedHotel = hotelService.saveHotel(hotelEntity);
//            return new ResponseEntity<>(savedHotel, HttpStatus.CREATED);
//        } catch (Exception e) {
//            return new ResponseEntity<>("Error saving hotel: " + e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
        return new ResponseEntity<>(hotelService.saveHotel(hotelEntity), HttpStatus.OK);
    }

    //Get the hotel by the hotel ID
    @GetMapping("/hotel/{id}")
    private ResponseEntity<?> getById(@PathVariable int id) {
        return new ResponseEntity<>(hotelService.getById(id), HttpStatus.OK);
    }

    //Get the hotel by the hotel name
    @GetMapping("/hotelByName/{name}")
    public ResponseEntity<?> getHotelByName(@PathVariable String name) {
        return new ResponseEntity<>(hotelService.getHotelByName(name), HttpStatus.OK);
    }

    //To partial update any field according to the hotel Id
    @PatchMapping("/updateHotel/{hotelId}")
    public ResponseEntity<Void> updateHotel(
            @PathVariable Integer hotelId,
            @RequestBody @Valid HotelUpdateDTO dto) {
        hotelService.updateHotelPartial(hotelId, dto);

        return ResponseEntity.noContent().build(); // 204
    }
    //Delete Hotel by Id
    @DeleteMapping("/deleteHotel/{hotelId}")
    public ResponseEntity<String> deleteHotel(@PathVariable Integer hotelId) {
        hotelService.deleteById(hotelId);
        return ResponseEntity.ok("Hotel with ID " + hotelId + " deleted successfully.");
    }

    // API to Upload Image
    // API to Upload Image
//    @PostMapping("/uploadImage/{hotelId}")
//    public ResponseEntity<String> uploadImage(@PathVariable Integer hotelId, @RequestParam("file") MultipartFile file) {
//        try {
//            hotelService.uploadImage(hotelId, file);
//            return ResponseEntity.ok("Image uploaded successfully!");
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image: " + e.getMessage());
//        }
//    }
//
//    // API to Fetch Image
//    @GetMapping("/getImage/{hotelId}")
//    public ResponseEntity<byte[]> getHotelImage(@PathVariable Integer hotelId) {
//        try {
//            byte[] imageData = hotelService.getImageByHotelId(hotelId);
//            return ResponseEntity.ok()
//                    .contentType(MediaType.IMAGE_JPEG)  // Set Content-Type as JPEG
//                    .body(imageData);
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//        }
//    }

    @PostMapping("/uploadImage/{hotelId}")
    public ResponseEntity<String> uploadImage(@PathVariable Integer hotelId, @RequestParam("file") MultipartFile file) {
        try {
            Hotel_Entity hotel = hotelService.uploadImage(hotelId, file);
            return ResponseEntity.ok("Image uploaded successfully! URL: " + hotel.getImageUrl());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image: " + e.getMessage());
        }
    }

    // Get Image URL by Hotel ID
    @GetMapping("/getImage/{hotelId}")
    public ResponseEntity<String> getHotelImage(@PathVariable Integer hotelId) {
        try {
            String imageUrl = hotelService.getImageByHotelId(hotelId);
            return ResponseEntity.ok(imageUrl);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found: " + e.getMessage());
        }
    }

    @PostMapping("/mine/hotels")
    public ResponseEntity<Hotel_Entity> createForMe(@RequestBody Hotel_Entity dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hotelService.saveHotelForCurrentUser(dto));
    }


    @PostMapping("/validate")
    public ResponseEntity<?> validateHotelData(@RequestBody Hotel_Entity hotel) {


        String hotelName = hotel.getName();

        List<Hotel_Entity> existing = hotelRepo.findAllByName(hotelName);

        if (!existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT) // 409 Conflict
                    .body(Map.of("message", "This hotel name already taken."));
        }

        // 🛡️ 2. Run your XSS/Logic checks here too!
        if (isMalicious(hotel)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Security Violation: Invalid characters detected."));
        }

        if (isLogicInvalid(hotel)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Data Violation: Price or Guest count is invalid."));
        }

        // If we reach here, the data is "Safe to Pay"
        return ResponseEntity.ok(Map.of("status", "valid"));
    }

    @PostMapping(value = "/mine/hotels", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Hotel_Entity> createForMe(
            @RequestPart("hotel") @NotNull String hotelJson,
            @RequestPart("imageUrl") MultipartFile coverImage,
            @RequestPart(value = "subImages", required = false) List<MultipartFile> subImages
    ) throws IOException {

        // 1. Convert JSON to Hotel_Entity
        Hotel_Entity hotel = objectMapper.readValue(hotelJson, Hotel_Entity.class);

        // ️ 2. XSS Security Check (Validate and Reject)
        if (isMalicious(hotel)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Security Violation: HTML or Scripts detected.");
        }

        //  3. Range & Logic Validation (The "Missing" Vulnerabilities)
        if (isLogicInvalid(hotel)) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data Violation: Price, Rating or Coordinates out of range.");
        }

        //  4. File Security Check (Size and Type)
        if (coverImage.getSize() > 2 * 1024 * 1024 || !coverImage.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only images allowed.");

        }

        // Upload cover image to Cloudinary
        Map coverResult = cloudinary.uploader().upload(coverImage.getBytes(), ObjectUtils.emptyMap());
        String coverUrl = coverResult.get("url").toString();
        hotel.setImageUrl(coverUrl); // Set the cover image URL

        // Upload subimages to Cloudinary, only if present
        List<String> subImageUrls = new ArrayList<>();
        if (subImages != null) {
            for (MultipartFile subImage : subImages) {
                Map uploadResult = cloudinary.uploader().upload(subImage.getBytes(), ObjectUtils.emptyMap());
                subImageUrls.add(uploadResult.get("url").toString());
            }
        }
        hotel.setSubImages(subImageUrls);  // Set the subimage URLs (could be empty)

        // Save hotel with the user information (handled by service)
        Hotel_Entity savedHotel = hotelService.saveHotelForCurrentUser(hotel);

        // Return the created hotel entity as the response
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHotel);
    }



    private boolean isMalicious(Hotel_Entity hotel) {
        String[] textFields = {
                hotel.getName(), hotel.getDestination(), hotel.getDescription(),
                hotel.getAddress(), hotel.getAccommodationType(), hotel.getReviews()
        };

        for (String field : textFields) {
            if (field != null) {
                String sanitized = XssSanitizer.sanitize(field);
                // If the sanitizer removed anything (like <script> or <div>), it's a violation
                if (!field.equals(sanitized)) return true;
            }
        }

        // Check Amenities List
        if (hotel.getAmenities() != null) {
            for (String amenity : hotel.getAmenities()) {
                if (!amenity.equals(XssSanitizer.sanitize(amenity))) return true;
            }
        }
        return false;
    }


    private boolean isLogicInvalid(Hotel_Entity hotel) {
        // 🛡️ Price: Must not be negative or suspiciously high
        if (hotel.getPrice() != null && (hotel.getPrice().doubleValue() < 0 || hotel.getPrice().doubleValue() > 100000)) return true;

        // 🛡️ Rating: Must be between 0 and 5
        if (hotel.getRating() != null && (hotel.getRating() < 0 || hotel.getRating() > 5)) return true;

        // 🛡️ Coordinates: Must be valid Lat/Lng
        if (hotel.getLat() != null && (hotel.getLat() < -90 || hotel.getLat() > 90)) return true;
        if (hotel.getLng() != null && (hotel.getLng() < -180 || hotel.getLng() > 180)) return true;

        // 🛡️ Guests/Rooms: Must be realistic
        if (hotel.getGuests() != null && (hotel.getGuests() < 1 || hotel.getGuests() > 20)) return true;

        return false;
    }



    @GetMapping("/mine/hotels")
    public List<Hotel_Entity> listMine() {
        return hotelService.getMyHotels();
    }

    @GetMapping("/mine/rooms")
    public List<Room> listMyRooms() {
        return hotelService.getMyRooms();
    }

    @PostMapping(value = "/mine/rooms", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Room addRoomForCurrentUser(
            @RequestPart("room") String roomJson,
            @RequestPart("file") MultipartFile file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        Room roomRequest = mapper.readValue(roomJson, Room.class);


        // 🛡️ 1. Security Gate: XSS & HTML Validation
        if (isMaliciousRoom(roomRequest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Security Violation: HTML or Scripts detected.");
        }

        // 🛡️ 2. Business Logic Gate: Numeric Validation
        if (isRoomLogicInvalid(roomRequest)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data Violation: Price or Room counts are invalid.");
        }

        // 🛡️ 3. File Security: Check image before processing
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file: Image required and must be under 5MB.");
        }

        return hotelService.addRoomForUser(roomRequest, file);
    }


    private boolean isMaliciousRoom(Room room) {
        String[] textFields = {
                room.getName(),
                room.getDescription(),
                room.getType()
        };

        for (String field : textFields) {
            if (field != null) {
                String sanitized = XssSanitizer.sanitize(field);
                // If the sanitized version doesn't match the original, it contained HTML/Scripts
                if (!field.equals(sanitized)) return true;
            }
        }
        return false;
    }

    private boolean isRoomLogicInvalid(Room room) {
        // Price cannot be negative or absurdly high (e.g., 1 billion)
        if (room.getPrice() < 0 || room.getPrice() > 1000000) return true;

        // Total rooms and available rooms must be logical
        if (room.getTotal() < 1 || room.getAvailable() < 0) return true;

        // Available rooms cannot exceed total rooms
        if (room.getAvailable() > room.getTotal()) return true;

        return false;
    }



}
