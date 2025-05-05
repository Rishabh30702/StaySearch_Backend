package com.example.StaySearch.StaySearchBackend.Hotels;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "*")
public class Hotel_Controller {

    @Autowired
    private Hotel_Service hotelService;

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
    public Hotel_Entity updateHotel(@PathVariable Integer hotelId, @RequestBody Hotel_Entity updatedHotel) {
        return hotelService.updateHotelPartial(hotelId, updatedHotel);
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

    @PostMapping(value = "/mine/hotels", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Hotel_Entity> createForMe(
            @RequestPart("hotel") @NotNull String hotelJson,
            @RequestPart("imageUrl") MultipartFile coverImage,
            @RequestPart(value = "subImages", required = false) List<MultipartFile> subImages
    ) throws IOException {

        // Convert JSON to Hotel_Entity object using the injected ObjectMapper
        Hotel_Entity hotel = objectMapper.readValue(hotelJson, Hotel_Entity.class);

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

        return hotelService.addRoomForUser(roomRequest, file);
    }




}
