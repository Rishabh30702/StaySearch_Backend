package com.example.StaySearch.StaySearchBackend.Hotels;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class Hotel_Service {

    @Autowired
    private Hotel_Repository hotelRepository;
    private static final String UPLOAD_DIR = "uploads/";

    //This is the function to fetch out all the hotel lists
    public List<Hotel_Entity> getAllHotels() {
        return hotelRepository.findAll();
    }

    //Function to save the data of hotels
    public Hotel_Entity saveHotel(Hotel_Entity hotel) {
        return hotelRepository.save(hotel);
    }

    ///Function to get the hotel by hotel id
    public Optional<Hotel_Entity> getById(int hotelId) {
        return hotelRepository.findById(hotelId);
    }

    //Function to find the hotel by the hotel name
    public Hotel_Entity getHotelByName(String hotel_name) {
        return hotelRepository.findByHotelName(hotel_name)
                .orElseThrow(() -> new RuntimeException("Hotel not found with name: " + hotel_name));
    }


    //This is the function to partial update the field
    @Transactional
    public Hotel_Entity updateHotelPartial(Integer hotelId, Hotel_Entity updatedHotel) {
        Optional<Hotel_Entity> optionalHotel = hotelRepository.findById(hotelId);
        if (optionalHotel.isPresent()) {
            Hotel_Entity existingHotel = optionalHotel.get();

            if (updatedHotel.getName() != null) {
                existingHotel.setName(updatedHotel.getName());
            }
            if (updatedHotel.getDescription() != null) {
                existingHotel.setDescription(updatedHotel.getDescription());
            }
            if (updatedHotel.getDestination() != null) {
                existingHotel.setDestination(updatedHotel.getDestination());
            }
            if (updatedHotel.getPrice() != null) {
                existingHotel.setPrice(updatedHotel.getPrice());
            }
            if (updatedHotel.getLat() != null) {
                existingHotel.setLat(updatedHotel.getLat());
            }
            if (updatedHotel.getLng() != null) {
                existingHotel.setLng(updatedHotel.getLng());
            }
            if (updatedHotel.getRating() != null) {
                existingHotel.setRating(updatedHotel.getRating());
            }
            if (updatedHotel.getReviews() != null) {
                existingHotel.setReviews(updatedHotel.getReviews());
            }
            if (updatedHotel.getLiked() != null) {
                existingHotel.setLiked(updatedHotel.getLiked());
            }
            if (updatedHotel.getAddress() != null) {
                existingHotel.setAddress(updatedHotel.getAddress());
            }
            if (updatedHotel.getCheckIn() != null) {
                existingHotel.setCheckIn(updatedHotel.getCheckIn());
            }
            if (updatedHotel.getCheckOut() != null) {
                existingHotel.setCheckOut(updatedHotel.getCheckOut());
            }
            if (updatedHotel.getGuests() != null) {
                existingHotel.setGuests(updatedHotel.getGuests());
            }
            if (updatedHotel.getRooms() != null) {
                existingHotel.setRooms(updatedHotel.getRooms());
            }
            if (updatedHotel.getImage() != null) {
                existingHotel.setImage(updatedHotel.getImage());
            }

            return hotelRepository.save(existingHotel);
        } else {
            throw new RuntimeException("Hotel not found with ID: " + hotelId);
        }
    }

    //This is the function to delete the record according to the hotel Id
    public void deleteById(Integer hotelId) {
        if (hotelRepository.existsById(hotelId)) {
            hotelRepository.deleteById(hotelId);
        } else {
            throw new RuntimeException("Hotel not found with ID: " + hotelId);
        }
    }
    // Upload Image
    public Hotel_Entity uploadImage(Integer hotelId, MultipartFile file) throws IOException {
        Hotel_Entity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with ID: " + hotelId));

        hotel.setImage(file.getBytes()); // Convert image to byte array
        return hotelRepository.save(hotel);
    }

    // Fetch Image by Hotel ID
    public byte[] getImageByHotelId(Integer hotelId) {
        Optional<Hotel_Entity> hotel = hotelRepository.findById(hotelId);
        if (hotel.isPresent() && hotel.get().getImage() != null) {
            return hotel.get().getImage();
        } else {
            throw new RuntimeException("Image not found for hotel ID: " + hotelId);
        }
    }
}
