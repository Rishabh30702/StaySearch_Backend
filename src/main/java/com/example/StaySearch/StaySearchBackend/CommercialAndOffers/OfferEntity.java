package com.example.StaySearch.StaySearchBackend.CommercialAndOffers;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "offers")
public class OfferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String badge;
    private String image;

    @Column(name = "valid_from", columnDefinition = "datetime")
    private LocalDateTime validFrom;

    @Column(name = "valid_till", columnDefinition = "datetime")
    private LocalDateTime validTill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.PENDING; // Default value

    public OfferEntity(Long id, String title, String description, String badge, String image,
                       LocalDateTime validFrom, LocalDateTime validTill, OfferStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.badge = badge;
        this.image = image;
        this.validFrom = validFrom;
        this.validTill = validTill;
        this.status = status;
    }

    public OfferEntity() {
    }

    // --- Getters and Setters ---

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDateTime validTill) {
        this.validTill = validTill;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }
}

