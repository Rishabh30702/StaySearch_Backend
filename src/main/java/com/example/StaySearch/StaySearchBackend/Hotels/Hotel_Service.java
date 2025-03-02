package com.example.StaySearch.StaySearchBackend.Hotels;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Hotel_Service {

    @Autowired
    private Hotel_Repository hotelRepository;

    //This is the function to fetch out all the hotel lists
    public List<Hotel_Entity> getAllHotels() {
        return hotelRepository.findAll();
    }
    //Function to save the data of hotels

    public Hotel_Entity saveHotel(Hotel_Entity hotel) {
        hotel.setVersion(0);  // Ensure initial version is set
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

            if (updatedHotel.getHotel_name() != null) {
                existingHotel.setHotel_name(updatedHotel.getHotel_name());
            }
            if (updatedHotel.getHotel_description() != null) {
                existingHotel.setHotel_description(updatedHotel.getHotel_description());
            }
            if (updatedHotel.getHotel_place() != null) {
                existingHotel.setHotel_place(updatedHotel.getHotel_place());
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
}
