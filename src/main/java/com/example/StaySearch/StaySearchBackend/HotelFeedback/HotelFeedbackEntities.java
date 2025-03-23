package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hotel_feedbacks")
public class HotelFeedbackEntities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_name", nullable = false)
    private String hotelName;

    @Column(name = "liked_amenities", columnDefinition = "TEXT")
    private String likedAmenities;  // Store as a comma-separated string

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

    // Constructors, getters, and setters

    public HotelFeedbackEntities() {
    }

    public HotelFeedbackEntities(Long id, String hotelName, String likedAmenities, int rating, String description, LocalDateTime createdAt) {
        this.id = id;
        this.hotelName = hotelName;
        this.likedAmenities = likedAmenities;
        this.rating = rating;
        this.description = description;
        this.createdAt = createdAt;
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

    public String getLikedAmenities() {
        return likedAmenities;
    }

    public void setLikedAmenities(String likedAmenities) {
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
}

