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
@RequestMapping("/api/informational-page")
public class InformationalPageController {

    @Autowired
    private InformationalPageRepository repository;

    @Autowired
    private Cloudinary cloudinary;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InformationalPage> createPage(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam MultipartFile image
    ) throws IOException {

        String imageUrl = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap())
                .get("url").toString();

        InformationalPage page = new InformationalPage();
        page.setTitle(title);
        page.setContent(content);
        page.setImageUrl(imageUrl);

        return ResponseEntity.ok(repository.save(page));
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
