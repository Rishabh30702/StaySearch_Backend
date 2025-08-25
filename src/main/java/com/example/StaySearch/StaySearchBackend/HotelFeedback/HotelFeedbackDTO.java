package com.example.StaySearch.StaySearchBackend.HotelFeedback;

import java.util.List;

public class HotelFeedbackDTO {
    private Integer hotelId;
    private String hotelName;
    private List<String> likedAmenities;
    private int rating;
    private String description;
    // getters & setters


    public HotelFeedbackDTO() {
    }

    public HotelFeedbackDTO(Integer hotelId, String hotelName, List<String> likedAmenities, int rating, String description) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.likedAmenities = likedAmenities;
        this.rating = rating;
        this.description = description;
    }

    public Integer getHotelId() {
        return hotelId;
    }

    public void setHotelId(Integer hotelId) {
        this.hotelId = hotelId;
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
}

