package com.example.StaySearch.StaySearchBackend.PlatformContentUpdate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "informationalpage")
public class InformationalPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    // This regex blocks common XSS attributes even if they don't have brackets
    @Pattern(
            // 1. (?i) makes it case-insensitive
            // 2. [<>\\{\\}\\[\\]] blocks tags and template braces
            // 3. (onerror|onload|javascript|alert|script|eval|srcdoc) blocks key XSS keywords
            regexp = "^(?i)(?!(.*([<>\\{\\}\\[\\]]|onerror|onload|javascript|alert|script|eval|srcdoc|iframe))).*$",
            message = "Input contains prohibited characters or security keywords"
    )
    private String title;

    @NotBlank(message = "Content is required")
    @Pattern(
            // 1. (?i) makes it case-insensitive
            // 2. [<>\\{\\}\\[\\]] blocks tags and template braces
            // 3. (onerror|onload|javascript|alert|script|eval|srcdoc) blocks key XSS keywords
            regexp = "^(?i)(?!(.*([<>\\{\\}\\[\\]]|onerror|onload|javascript|alert|script|eval|srcdoc|iframe))).*$",
            message = "Input contains prohibited characters or security keywords"
    )
    @Size(min = 10, max = 5000, message = "Content must be between 10 and 5000 characters")
    // We allow standard text but block scripts specifically
    private String content;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    public InformationalPage(Long id, String title, String content, String imageUrl) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public InformationalPage() {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}