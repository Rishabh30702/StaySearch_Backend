package com.example.StaySearch.StaySearchBackend.Hotels;

public class RoomRequset {
    public class RoomRequest {
        public String name;
        public String description;
        public String imageUrl;
        public String type;
        public double price;
        public int total;
        public int available;
        public boolean deal;
        public Integer hotelId; // ID of hotel owned by logged-in user
    }
}
