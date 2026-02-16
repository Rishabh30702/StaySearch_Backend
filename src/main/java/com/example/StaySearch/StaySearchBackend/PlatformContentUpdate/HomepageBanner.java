package com.example.StaySearch.StaySearchBackend.PlatformContentUpdate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "homepagebanner")
public class HomepageBanner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    // Restricting to 3-50 characters to prevent database bloat or UI breaking
    @Size(min = 3, max = 50, message = "Title must be between 3 and 50 characters")
    // Optional: Allow only alphanumeric, spaces, and basic punctuation
    @Pattern(regexp = "^[a-zA-Z0-20\\s.,!'-]*$", message = "Title contains illegal characters")
    private String title;

    @NotBlank(message = "Image URL is required")
    @Size(max = 255, message = "URL is too long")
    private String imageUrl;

    public HomepageBanner(Long id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public HomepageBanner() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
