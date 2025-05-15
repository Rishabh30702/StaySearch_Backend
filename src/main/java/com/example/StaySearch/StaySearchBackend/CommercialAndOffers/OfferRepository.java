package com.example.StaySearch.StaySearchBackend.CommercialAndOffers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<OfferEntity, Long> {

    // Delete expired offers
    void deleteByValidTillBefore(LocalDateTime now);

    // Get all valid offers regardless of approval
    List<OfferEntity> findByValidFromBeforeAndValidTillAfter(LocalDateTime now1, LocalDateTime now2);

    // ✅ Get active and approved offers (for users)
    List<OfferEntity> findByStatusAndValidFromBeforeAndValidTillAfter(OfferStatus status, LocalDateTime now1, LocalDateTime now2);


    // ✅ Get offers by approval status (for admin dashboard)
    List<OfferEntity> findByStatus(OfferStatus status);
}
