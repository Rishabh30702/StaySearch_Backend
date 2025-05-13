package com.example.StaySearch.StaySearchBackend.CommercialAndOffers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    @Autowired
    private OfferService offerService;

    @GetMapping("/active")
    public List<OfferEntity> getActiveOffers() {
        return offerService.getActiveOffers();
    }

    @PostMapping
    public OfferEntity createOffer(@RequestBody OfferEntity offer) {
        return offerService.createOffer(offer);
    }

    // ✅ Get all offers (active or expired)
    @GetMapping
    public List<OfferEntity> getAllOffers() {
        return offerService.getAllOffers();
    }

    // ✅ Get an offer by ID
    @GetMapping("/{id}")
    public OfferEntity getOfferById(@PathVariable Long id) {
        return offerService.getOfferById(id);
    }


    // ✅ Update an existing offer
    @PutMapping("/{id}")
    public OfferEntity updateOffer(@PathVariable Long id, @RequestBody OfferEntity updatedOffer) {
        return offerService.updateOffer(id, updatedOffer);
    }

    // ✅ Delete an offer by ID
    @DeleteMapping("/{id}")
    public void deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
    }
}
