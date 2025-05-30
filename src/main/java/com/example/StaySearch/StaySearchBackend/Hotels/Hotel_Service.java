package com.example.StaySearch.StaySearchBackend.Hotels;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.StaySearch.StaySearchBackend.Exception.ResourceNotFoundException;
import com.example.StaySearch.StaySearchBackend.JWT.User;
import com.example.StaySearch.StaySearchBackend.JWT.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class Hotel_Service {

    @Autowired
    private Hotel_Repository hotelRepository;
    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private  RoomRepository roomRepository;

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
    public Hotel_Entity getById(int hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));
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
            if (updatedHotel.getImageUrl() != null) {
                existingHotel.setImageUrl(updatedHotel.getImageUrl());
            }
            if (updatedHotel.getAmenities() != null) {
                existingHotel.setAmenities(updatedHotel.getAmenities());
            }
            if (updatedHotel.getRoomsList() != null) {
                // Clear the existing rooms if you're replacing them
                existingHotel.getRoomsList().clear();

                // Loop through incoming rooms and bind them to this hotel
                for (Room room : updatedHotel.getRoomsList()) {
                    room.setHotel(existingHotel); // important!
                    existingHotel.getRoomsList().add(room);
                }
            }
            if (updatedHotel.getAccommodationType() != null) {
                existingHotel.setAccommodationType(updatedHotel.getAccommodationType());
            }
            if (updatedHotel.getSubImages() != null) {
                existingHotel.setSubImages(updatedHotel.getSubImages());
            }
//            if (updatedHotel.getImageUrl() != null) {
//                existingHotel.setImageUrl(updatedHotel.getImageUrl());
//            }

            // Optional (only if needed and properly secured)
        /*
        if (updatedHotel.getLikedByUsers() != null) {
            existingHotel.setLikedByUsers(updatedHotel.getLikedByUsers());
        }
        if (updatedHotel.getUser() != null) {
            existingHotel.setUser(updatedHotel.getUser());
        }
        */

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
//    public Hotel_Entity uploadImage(Integer hotelId, MultipartFile file) throws IOException {
//        Hotel_Entity hotel = hotelRepository.findById(hotelId)
//                .orElseThrow(() -> new RuntimeException("Hotel not found with ID: " + hotelId));
//
//        hotel.setImage(file.getBytes()); // Convert image to byte array
//        return hotelRepository.save(hotel);
//    }
//
//    // Fetch Image by Hotel ID
//    public byte[] getImageByHotelId(Integer hotelId) {
//        Optional<Hotel_Entity> hotel = hotelRepository.findById(hotelId);
//        if (hotel.isPresent() && hotel.get().getImage() != null) {
//            return hotel.get().getImage();
//        } else {
//            throw new RuntimeException("Image not found for hotel ID: " + hotelId);
//        }
//    }

    public Hotel_Entity uploadImage(Integer hotelId, MultipartFile file) throws IOException {
        Hotel_Entity hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found with ID: " + hotelId));

        // Upload image to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String imageUrl = uploadResult.get("url").toString();  // Get the URL of the uploaded image

        // Save the image URL instead of binary data
        hotel.setImageUrl(imageUrl);
        return hotelRepository.save(hotel);
    }

    // Fetch Image URL by Hotel ID
    public String getImageByHotelId(Integer hotelId) {
        Optional<Hotel_Entity> hotel = hotelRepository.findById(hotelId);
        if (hotel.isPresent() && hotel.get().getImageUrl() != null) {
            return hotel.get().getImageUrl();
        } else {
            throw new RuntimeException("Image not found for hotel ID: " + hotelId);
        }
    }

    public String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        System.out.println("📢 currentUsername() called → " + username);
        return username;
    }

    @Transactional
    public Hotel_Entity saveHotelForCurrentUser(Hotel_Entity hotel) {
        // Fetch the user from the repository
        User owner = userRepository.findByUsername(currentUsername())
                .orElseThrow(() -> new UsernameNotFoundException(currentUsername()));

        // Check if a hotel with the same name already exists for this user
        Optional<Hotel_Entity> existingHotel = hotelRepository.findByUserAndName(owner, hotel.getName());
        if (existingHotel.isPresent()) {
            throw new RuntimeException("Hotel with this name already exists for this user.");
        }

        hotel.setUser(owner);  // Set the current user as the hotel owner

        return hotelRepository.save(hotel);  // Save the hotel to the repository
    }


    /* NEW read‑my‑hotels */
    @Transactional(readOnly = true)
    public List<Hotel_Entity> getMyHotels() {
        List<Hotel_Entity> hotels = hotelRepository.findByUserUsername(currentUsername());
        System.out.println("Fetched Hotels: " + hotels.size());
        return hotels;
    }

    @Transactional(readOnly = true)
    public List<Room> getMyRooms() {
        String username = getCurrentUsername(); // Replace with your actual username fetch method
        List<Room> rooms = roomRepository.findByHotelUserUsername(username);
        System.out.println("Fetched Rooms: " + rooms.size());
        return rooms;
    }

    private String getCurrentUsername() {
        // Example: If you're using Spring Security
        return org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
    }

    @Transactional
    public Room addRoomForUser(Room request, MultipartFile file) throws IOException {
        String username = getCurrentUsername();

        Hotel_Entity hotel = hotelRepository.findById(request.getHotelId())
                .filter(h -> h.getUser().getUsername().equals(username))
                .orElseThrow(() -> new RuntimeException("Hotel not found or does not belong to user"));

        Room room = new Room();
        room.setName(request.getName());
        room.setDescription(request.getDescription());
        room.setType(request.getType());
        room.setPrice(request.getPrice());
        room.setTotal(request.getTotal());
        room.setAvailable(request.getAvailable());
        room.setDeal(request.isDeal());
        room.setHotel(hotel);

        // Upload image to Cloudinary
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String imageUrl = uploadResult.get("url").toString();  // Get the URL of the uploaded image

        // Save the image URL
        room.setImageUrl(imageUrl);

        return roomRepository.save(room);
    }


}