package com.example.StaySearch.StaySearchBackend.Hotels;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("v1")
public class Hotel_Controller {

    @Autowired
    private Hotel_Service hotelService;

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
    @PostMapping("/uploadImage/{hotelId}")
    public ResponseEntity<String> uploadImage(@PathVariable Integer hotelId, @RequestParam("file") MultipartFile file) {
        try {
            hotelService.uploadImage(hotelId, file);
            return ResponseEntity.ok("Image uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image: " + e.getMessage());
        }
    }

    // API to Fetch Image
    @GetMapping("/getImage/{hotelId}")
    public ResponseEntity<byte[]> getHotelImage(@PathVariable Integer hotelId) {
        try {
            byte[] imageData = hotelService.getImageByHotelId(hotelId);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)  // Set Content-Type as JPEG
                    .body(imageData);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


}
