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
@RequestMapping("/api/promotional-material")
public class PromotionalMaterialController {

    @Autowired
    private PromotionalMaterialRepository repository;

    @Autowired
    private Cloudinary cloudinary;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PromotionalMaterial> createMaterial(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam MultipartFile image
    ) throws IOException {

        String imageUrl = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap())
                .get("url").toString();

        PromotionalMaterial material = new PromotionalMaterial();
        material.setTitle(title);
        material.setContent(content);
        material.setImageUrl(imageUrl);

        return ResponseEntity.ok(repository.save(material));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PromotionalMaterial> updateMaterial(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image
    ) throws IOException {
        Optional<PromotionalMaterial> optional = repository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PromotionalMaterial material = optional.get();
        material.setTitle(title);
        material.setContent(content);

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap()).get("url").toString();
            material.setImageUrl(imageUrl);
        }

        return ResponseEntity.ok(repository.save(material));
    }
    @GetMapping
    public ResponseEntity<List<PromotionalMaterial>> getAllMaterials() {
        return ResponseEntity.ok(repository.findAll());
    }

}