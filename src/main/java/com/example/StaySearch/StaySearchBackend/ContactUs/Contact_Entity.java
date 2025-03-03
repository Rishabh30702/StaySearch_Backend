package com.example.StaySearch.StaySearchBackend.ContactUs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.*;

@Entity
@Table(name = "contactus")
public class Contact_Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_id")
    private Integer contact_id;  // Change int to Integer

    @Column(name = "contact_username")
    private String contact_username;
    @Column(name = "contact_description")
    private String contact_description;
    @Column(name = "contact_subject")
    private String 	contact_subject;

    @Version
    @JsonIgnore  // Ignore version in requests
    @Column(nullable = false)
    private Integer version = 0;

    public Contact_Entity() {
    }

    public Contact_Entity(String contact_username, String contact_description, String contact_subject, Integer version) {
        this.contact_username = contact_username;
        this.contact_description = contact_description;
        this.contact_subject = contact_subject;
        this.version = version;
    }

    public Integer getContact_id() {
        return contact_id;
    }

    public void setContact_id(Integer contact_id) {
        this.contact_id = contact_id;
    }

    public String getContact_username() {
        return contact_username;
    }

    public void setContact_username(String contact_username) {
        this.contact_username = contact_username;
    }

    public String getContact_description() {
        return contact_description;
    }

    public void setContact_description(String contact_description) {
        this.contact_description = contact_description;
    }

    public String getContact_subject() {
        return contact_subject;
    }

    public void setContact_subject(String contact_subject) {
        this.contact_subject = contact_subject;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
