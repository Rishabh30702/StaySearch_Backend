package com.example.StaySearch.StaySearchBackend.Hotels;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.TableGenerator;

@Entity
@Table(name="hotels")
public class Hotel_Entity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hotel_id")
    private Integer hotel_id;  // Change int to Integer

    @Column(name = "hotel_name")
    private String hotel_name;
    @Column(name = "hotel_description")
    private String hotel_description;
    @Column(name = "hotel_place")
    private String 	hotel_place;

    @Version
    @JsonIgnore  // Ignore version in requests
    @Column(nullable = false)
    private Integer version = 0;

    public Hotel_Entity() {
    }

    public Hotel_Entity(String hotel_name, String hotel_description, String hotel_place) {
        this.hotel_name = hotel_name;
        this.hotel_description = hotel_description;
        this.hotel_place = hotel_place;
    }


    public int getHotel_id() {
        return hotel_id;
    }

    public void setHotel_id(int hotel_id) {
        this.hotel_id = hotel_id;
    }

    public String getHotel_name() {
        return hotel_name;
    }

    public void setHotel_name(String hotel_name) {
        this.hotel_name = hotel_name;
    }

    public String getHotel_description() {
        return hotel_description;
    }

    public void setHotel_description(String hotel_description) {
        this.hotel_description = hotel_description;
    }

    public String getHotel_place() {
        return hotel_place;
    }

    public void setHotel_place(String hotel_place) {
        this.hotel_place = hotel_place;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
