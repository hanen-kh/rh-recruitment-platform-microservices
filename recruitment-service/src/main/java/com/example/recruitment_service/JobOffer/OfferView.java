package com.example.recruitment_service.JobOffer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record OfferView(UUID id, String title, LocalDateTime publishedAt,
                        LocalDate applicationDeadline, boolean open) {

    public static OfferView from(JobOffer offer, LocalDate today) {
        return new OfferView(offer.getId(), offer.getTitle(), offer.getPublishedAt(),
                offer.getApplicationDeadline(), offer.isOpenForApplications(today));
    }
}
