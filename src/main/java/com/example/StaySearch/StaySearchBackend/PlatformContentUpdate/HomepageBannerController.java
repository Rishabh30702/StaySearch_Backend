package com.example.StaySearch.StaySearchBackend.PlatformContentUpdate;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/homepage-banner")
public class HomepageBannerController {

    @Autowired
    private HomepageBannerRepository repository;

    @Autowired
    private Cloudinary cloudinary;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HomepageBanner> createBanner(
            @RequestParam String title,
            @RequestParam("images") MultipartFile[] images
    ) throws IOException {

        if (images.length != 2) {
            return ResponseEntity.badRequest().body(null); // Or throw a custom exception
        }

        String imageUrl1 = cloudinary.uploader().upload(images[0].getBytes(), ObjectUtils.emptyMap())
                .get("url").toString();
        String imageUrl2 = cloudinary.uploader().upload(images[1].getBytes(), ObjectUtils.emptyMap())
                .get("url").toString();

        HomepageBanner banner = new HomepageBanner();
        banner.setTitle(title);
        banner.setImageUrl1(imageUrl1);  // Make sure your entity has these two fields
        banner.setImageUrl2(imageUrl2);

        return ResponseEntity.ok(repository.save(banner));
    }
    // Add GET, PUT, DELETE if needed
    @GetMapping
    public ResponseEntity<List<HomepageBanner>> getAllBanners() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HomepageBanner> updateBanner(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(name = "images", required = false) MultipartFile[] images
    ) throws IOException {
        Optional<HomepageBanner> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HomepageBanner banner = optional.get();
        banner.setTitle(title);

        if (images != null) {
            if (images.length > 2) {
                return ResponseEntity.badRequest().build(); // Reject more than 2 images
            }

            if (images.length >= 1 && images[0] != null && !images[0].isEmpty()) {
                String imageUrl1 = cloudinary.uploader().upload(images[0].getBytes(), ObjectUtils.emptyMap())
                        .get("url").toString();
                banner.setImageUrl1(imageUrl1);
            }

            if (images.length == 2 && images[1] != null && !images[1].isEmpty()) {
                String imageUrl2 = cloudinary.uploader().upload(images[1].getBytes(), ObjectUtils.emptyMap())
                        .get("url").toString();
                banner.setImageUrl2(imageUrl2);
            }
        }

        return ResponseEntity.ok(repository.save(banner));
    }

}
