package com.example.StaySearch.StaySearchBackend.CommercialAndOffers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OfferService {
    @Autowired
    private OfferRepository offerRepository;

    public List<OfferEntity> getActiveOffers() {
        LocalDateTime now = LocalDateTime.now();
        return offerRepository.findByStatusAndValidFromBeforeAndValidTillAfter(OfferStatus.APPROVED, now, now);
    }


    public OfferEntity createOffer(OfferEntity offer) {
        return offerRepository.save(offer);
    }

    @Transactional
    public void deleteExpiredOffers() {
        LocalDateTime now = LocalDateTime.now();
        offerRepository.deleteByValidTillBefore(now);
    }

    public List<OfferEntity> getAllOffers() {
        return offerRepository.findAll();
    }

    public OfferEntity getOfferById(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found with ID: " + id));
    }

    public OfferEntity updateOffer(Long id, OfferEntity updatedOffer) {
        OfferEntity existing = getOfferById(id);
        existing.setTitle(updatedOffer.getTitle());
        existing.setDescription(updatedOffer.getDescription());
        existing.setBadge(updatedOffer.getBadge());
        existing.setImage(updatedOffer.getImage());
        existing.setValidFrom(updatedOffer.getValidFrom());
        existing.setValidTill(updatedOffer.getValidTill());
        existing.setStatus(updatedOffer.getStatus()); // ✅ Include status
        return offerRepository.save(existing);
    }


    @Transactional
    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Offer not found with ID: " + id);
        }
        offerRepository.deleteById(id);
    }

    // ✅ Update approval status (approve/reject)
    public OfferEntity updateOfferStatus(Long id, OfferStatus status) {
        OfferEntity offer = getOfferById(id);
        offer.setStatus(status);
        return offerRepository.save(offer);
    }

    // ✅ Get all offers by status (for admin moderation)
    public List<OfferEntity> getOffersByStatus(OfferStatus status) {
        return offerRepository.findByStatus(status);
    }
}
