package com.example.StaySearch.StaySearchBackend.JWT;

import com.example.StaySearch.StaySearchBackend.Hotels.Hotel_Entity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


// Changes username to accept only valid email formats
    @NotBlank(message = "Email/Username is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 50, message = "Email is too long")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password is required")

    private String password;


    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Full name contains illegal characters")
    @Column(name = "fullname")
    private String fullname;


    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10-15 digits")
    @Column(name = "phone")
    private String phonenumber;

    private String role;

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

    @Column(name = "rejection_remark")
    private String rejectionRemark;

    @Column(name = "password_changed_at")
    private Long passwordLastChangedAt = System.currentTimeMillis();

    public Long getPasswordLastChangedAt() {
        return passwordLastChangedAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // This wraps your string role into a SimpleGrantedAuthority object
        // and puts it into a List (which is a type of Collection)
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Set to true so the user can log in
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Set to true so the user can log in
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Set to true so the user can log in
    }

    @Override
    public boolean isEnabled() {
        return true; // Set to true so the user can log in
    }

    public void setPasswordLastChangedAt(Long passwordLastChangedAt) {
        this.passwordLastChangedAt = passwordLastChangedAt;
    }


    @Transient
    private boolean mailFailed;

    public boolean isMailFailed() { return mailFailed; }
    public void setMailFailed(boolean mailFailed) { this.mailFailed = mailFailed; }




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

    public String getRejectionRemark() {
        return rejectionRemark;
    }

    public void setRejectionRemark(String rejectionRemark) {
        this.rejectionRemark = rejectionRemark;
    }
}
