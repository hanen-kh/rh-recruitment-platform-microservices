package com.example.recruitment_service.JobOffer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobOfferRepository extends JpaRepository<JobOffer, UUID> {

    List<JobOffer> findByStatusOrderByPublishedAtDesc(JobOfferStatus status);

    List<JobOffer> findByStatusAndDepartmentIgnoreCaseOrderByPublishedAtDesc(
            JobOfferStatus status,
            String department
    );

    List<JobOffer> findByStatusAndTitleContainingIgnoreCaseOrderByPublishedAtDesc(
            JobOfferStatus status,
            String title
    );

    List<JobOffer> findByCreatedByIdOrderByCreatedAtDesc(String createdById);
}
