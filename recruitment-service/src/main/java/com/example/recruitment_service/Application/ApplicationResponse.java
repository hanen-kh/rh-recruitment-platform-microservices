package com.example.recruitment_service.Application;

import com.example.recruitment_service.JobOffer.OfferView;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ApplicationResponse(UUID id, String candidatId, OfferView offer, ApplicationStatus status,
                                  String coverLetter, Instant submittedAt, Instant updatedAt) {

    public static ApplicationResponse from(JobApplication a, LocalDate today) {
        return new ApplicationResponse(a.getId(), a.getCandidateId(), OfferView.from(a.getOffer(),today),a.getStatus(), a.getCoverLetter(), a.getSubmittedAt(), a.getUpdatedAt());
    }
}
