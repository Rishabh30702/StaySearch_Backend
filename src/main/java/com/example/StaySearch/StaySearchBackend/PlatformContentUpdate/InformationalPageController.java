package com.example.StaySearch.StaySearchBackend.PlatformContentUpdate;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.StaySearch.StaySearchBackend.Security.XssSanitizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/informational-page")
public class InformationalPageController {

    @Autowired
    private InformationalPageRepository repository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired // 2. Add this annotation
    private Validator validator;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPage(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam MultipartFile image
    ) {
        try {
            // Step 1: Sanitize (removes tags, leaves keywords)
            String safeTitle = XssSanitizer.sanitize(title);
            String safeContent = XssSanitizer.sanitize(content);

            InformationalPage page = new InformationalPage();
            page.setTitle(safeTitle);
            page.setContent(safeContent);

            // Step 2: Manually Run Validator
            // This checks the safeTitle against our new Hardened Regex
            var violations = validator.validate(page);
            if (!violations.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Security Violation: " + violations.iterator().next().getMessage());
            }

            // Step 3: Save only if clean
            return ResponseEntity.ok(repository.save(page));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }


    @GetMapping
    public ResponseEntity<List<InformationalPage>> getAllPages() {
        return ResponseEntity.ok(repository.findAll());
    }
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InformationalPage> updatePage(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image
    ) throws IOException {
        Optional<InformationalPage> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        InformationalPage page = optional.get();
        page.setTitle(title);
        page.setContent(content);

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap()).get("url").toString();
            page.setImageUrl(imageUrl);
        }

        return ResponseEntity.ok(repository.save(page));
    }

}
