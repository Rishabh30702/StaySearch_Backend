package com.example.StaySearch.StaySearchBackend.Hotels;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    private Hotel_Entity hotel;

    // ✅ Additional Fields (based on frontend payload)
    @Column(name = "type")
    private String type;

    @Column(name = "price")
    private double price;

    @Column(name = "total")
    private int total;

    @Column(name = "available")
    private int available;

    @Column(name = "deal")
    private boolean deal;

    // Constructors
    public Room() {}

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public boolean isDeal() {
        return deal;
    }

    public void setDeal(boolean deal) {
        this.deal = deal;
    }

}