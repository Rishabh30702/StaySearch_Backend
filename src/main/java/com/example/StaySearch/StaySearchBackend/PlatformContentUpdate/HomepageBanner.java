package com.example.StaySearch.StaySearchBackend.PlatformContentUpdate;

import jakarta.persistence.*;

@Entity
@Table(name = "homepagebanner")
public class HomepageBanner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String imageUrl1;
    private String imageUrl2;

    public HomepageBanner(Long id, String title, String imageUrl1, String imageUrl2) {
        this.id = id;
        this.title = title;
        this.imageUrl1 = imageUrl1;
        this.imageUrl2 = imageUrl2;
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

    public String getImageUrl1() {
        return imageUrl1;
    }

    public void setImageUrl1(String imageUrl1) {
        this.imageUrl1 = imageUrl1;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }

    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }
}
