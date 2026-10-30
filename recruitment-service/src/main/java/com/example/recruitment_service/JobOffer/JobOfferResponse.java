package com.example.recruitment_service.JobOffer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobOfferResponse(
        UUID id,
        String title,
        String description,
        String requirements,
        String department,
        String location,
        ContractType contractType,
        WorkMode workMode,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String salaryCurrency,
        Integer openings,
        JobOfferStatus status,
        LocalDate applicationDeadline,
        LocalDateTime publishedAt,
        String createdById,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static JobOfferResponse fromEntity(JobOffer offer) {
        return new JobOfferResponse(
                offer.getId(), offer.getTitle(), offer.getDescription(), offer.getRequirements(),
                offer.getDepartment(), offer.getLocation(), offer.getContractType(), offer.getWorkMode(),
                offer.getSalaryMin(), offer.getSalaryMax(), offer.getSalaryCurrency(), offer.getOpenings(),
                offer.getStatus(), offer.getApplicationDeadline(), offer.getPublishedAt(),
                offer.getCreatedById(), offer.getCreatedAt(), offer.getUpdatedAt()
        );
    }
}
