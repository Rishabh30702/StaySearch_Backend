package com.example.StaySearch.StaySearchBackend.CommercialAndOffers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<OfferEntity, Long> {

    // Custom query to delete expired offers
    void deleteByValidTillBefore(LocalDateTime now);

    // Get only currently valid offers
    List<OfferEntity> findByValidFromBeforeAndValidTillAfter(LocalDateTime now1, LocalDateTime now2);
}