package com.example.StaySearch.StaySearchBackend.JWT;

import com.example.StaySearch.StaySearchBackend.Hotels.Hotel_Entity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String role;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "phone")
    private String phonenumber;

    @ManyToMany
    @JoinTable(
            name = "user_wishlist",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "hotel_id")
    )
    private List<Hotel_Entity> wishlist = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Hotel_Entity> hotels = new ArrayList<>();

    public List<Hotel_Entity> getHotels() {
        return hotels;
    }

    public void setHotels(List<Hotel_Entity> hotels) {
        this.hotels = hotels;
    }

    @Column(name = "status", nullable = true)
    private String status; // Only used for hoteliers

    public User() {
    }


    public User(Long id, String username, String password, String role, String fullname, String phonenumber, List<Hotel_Entity> wishlist, List<Hotel_Entity> hotels, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullname = fullname;
        this.phonenumber = phonenumber;
        this.wishlist = wishlist;
        this.hotels = hotels;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public List<Hotel_Entity> getWishlist() {
        return wishlist;
    }

    public void setWishlist(List<Hotel_Entity> wishlist) {
        this.wishlist = wishlist;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
