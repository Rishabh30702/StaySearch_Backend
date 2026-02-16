package com.example.StaySearch.StaySearchBackend.Hotels;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class HotelUpdateDTO {

    @Size(max = 255)

    @Pattern(
            regexp = "^[a-zA-Z0-9 .,'-]+$",
            message = "Invalid characters in name"
    )
    private String name;

    @Size(max = 500)
    @Pattern(
            regexp = "^[a-zA-Z0-9 .,'\\-/]+$",
            message = "Invalid characters in address"
    )

    @Size(max = 500)
    private String address;

    @Size(max = 100)
    @Size(max = 100)
    @Pattern(
            regexp = "^[a-zA-Z ]+$",
            message = "Invalid accommodation type"
    )
    private String accommodationType;


    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9 .,:'\"/\\-()\\n\\r]*$",
            message = "Description contains invalid characters"
    )
    private String description;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAccommodationType() {
        return accommodationType;
    }

    public void setAccommodationType(String accommodationType) {
        this.accommodationType = accommodationType;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
