package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import com.example.StaySearch.StaySearchBackend.Hotels.Hotel_Entity;
import com.example.StaySearch.StaySearchBackend.JWT.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "hotel_feedbacks")
public class HotelFeedbackEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_name", nullable = false)
    private String hotelName;

    @ElementCollection
    @CollectionTable(name = "liked_amenities", joinColumns = @JoinColumn(name = "feedback_id"))
    @Column(name = "amenity")
    private List<String> likedAmenities; // Store amenities as an array

    @Column(nullable = false)
    private int rating; // Star rating (1 to 5)

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    @ManyToOne
    @JoinColumn(name = "user_id") // ✅ this creates a real foreign key column in hotel_feedbacks
    @JsonIgnoreProperties({"password", "roles", "authorities", "wishlist", "hotels"})
    private User user;


    @ManyToOne
    @JoinColumn(name = "hotel_id")
    @JsonIgnore
    private Hotel_Entity hotel;

    @Enumerated(EnumType.STRING)
    private FeedbackStatus status;

    // Constructors, getters, and setters

    public HotelFeedbackEntities() {
    }

    public HotelFeedbackEntities(Long id, String hotelName, List<String> likedAmenities, int rating, String description, LocalDateTime createdAt, User user, Hotel_Entity hotel, FeedbackStatus status) {
        this.id = id;
        this.hotelName = hotelName;
        this.likedAmenities = likedAmenities;
        this.rating = rating;
        this.description = description;
        this.createdAt = createdAt;
        this.user = user;
        this.hotel = hotel;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public List<String> getLikedAmenities() {
        return likedAmenities;
    }

    public void setLikedAmenities(List<String> likedAmenities) {
        this.likedAmenities = likedAmenities;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Hotel_Entity getHotel() {
        return hotel;
    }

    public void setHotel(Hotel_Entity hotel) {
        this.hotel = hotel;
    }

    public FeedbackStatus getStatus() {
        return status;
    }

    public void setStatus(FeedbackStatus status) {
        this.status = status;
    }
}

