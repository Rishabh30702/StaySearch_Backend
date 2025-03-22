package com.example.StaySearch.StaySearchBackend.Hotels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "hotels")
public class Hotel_Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hotel_id")
    private Integer hotelId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 10, scale = 2) // Storing price as decimal
    private BigDecimal price;

    @Column(name = "image_url")  // Store image as URL instead of BLOB
    private String imageUrl;


    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Column(name = "rating")
    private Float rating;

    @Column(name = "reviews")
    private String reviews;

    @Column(name = "liked")
    private Boolean liked;

    @Column(name = "address")
    private String address;

    @Column(name = "check_in")
    private LocalDate checkIn;

    @Column(name = "check_out")
    private LocalDate checkOut;

    @Column(name = "guests")
    private Integer guests;

    @Column(name = "rooms")
    private Integer rooms;

    @Version
    @JsonIgnore
    @Column(nullable = false)
    private Integer version = 0;

    @ElementCollection
    @CollectionTable(name = "hotel_amenities", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "amenity")
    private List<String> amenities;

    // 🆕 ADD SUB-IMAGES LIST
    @ElementCollection
    @CollectionTable(name = "hotel_sub_images", joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "sub_image_url")
    private List<String> subImages;

    // 🆕 ADD ONE-TO-MANY RELATIONSHIP WITH ROOMS
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> roomsList;

    // Default Constructor
    public Hotel_Entity() {
    }

    // Constructor with all fields
    public Hotel_Entity(String name, String destination, String description, BigDecimal price, String imageUrl,
                        Double lat, Double lng, Float rating, String reviews, Boolean liked, String address,
                        LocalDate checkIn, LocalDate checkOut, Integer guests, Integer rooms,
                        List<String> amenities, List<String> subImages, List<Room> roomsList) {
        this.name = name;
        this.destination = destination;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
        this.reviews = reviews;
        this.liked = liked;
        this.address = address;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.guests = guests;
        this.rooms = rooms;
        this.amenities = amenities;
        this.subImages = subImages;  // ✅ Add sub-images
        this.roomsList = roomsList;  // ✅ Add rooms list
    }


    // Getters and Setters
    public Integer getHotelId() {
        return hotelId;
    }

    public void setHotelId(Integer hotelId) {
        this.hotelId = hotelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getReviews() {
        return reviews;
    }

    public void setReviews(String reviews) {
        this.reviews = reviews;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public Integer getGuests() {
        return guests;
    }

    public void setGuests(Integer guests) {
        this.guests = guests;
    }

    public Integer getRooms() {
        return rooms;
    }

    public void setRooms(Integer rooms) {
        this.rooms = rooms;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }
    public List<String> getSubImages() {
        return subImages;
    }

    public void setSubImages(List<String> subImages) {
        this.subImages = subImages;
    }

    public List<Room> getRoomsList() {
        return roomsList;
    }

    public void setRoomsList(List<Room> roomsList) {
        this.roomsList = roomsList;
    }
}
