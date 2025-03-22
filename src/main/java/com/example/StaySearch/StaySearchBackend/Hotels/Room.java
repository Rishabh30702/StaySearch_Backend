package com.example.StaySearch.StaySearchBackend.Hotels;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel_Entity hotel;

    // Default Constructor
    public Room() {}

    // Constructor
    public Room(String name, String description, String imageUrl, Hotel_Entity hotel) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.hotel = hotel;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Hotel_Entity getHotel() {
        return hotel;
    }

    public void setHotel(Hotel_Entity hotel) {
        this.hotel = hotel;
    }
}
