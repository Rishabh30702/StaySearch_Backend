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
            @RequestParam MultipartFile image
    ) throws IOException {

        String imageUrl = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap())
                .get("url").toString();

        HomepageBanner banner = new HomepageBanner();
        banner.setTitle(title);
        banner.setImageUrl(imageUrl);

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
            @RequestParam(required = false) MultipartFile image
    ) throws IOException {
        Optional<HomepageBanner> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HomepageBanner banner = optional.get();
        banner.setTitle(title);

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap()).get("url").toString();
            banner.setImageUrl(imageUrl);
        }

        return ResponseEntity.ok(repository.save(banner));
    }
}
