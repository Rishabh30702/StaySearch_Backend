package com.example.StaySearch.StaySearchBackend.CommercialAndOffers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerConfig {

    @Autowired
    private OfferService offerService;

//    @Scheduled(cron = "0 0 * * * *")
@Scheduled(fixedRate = 30000)
    public void deleteExpiredOffers() {
        offerService.deleteExpiredOffers();
    }
}
