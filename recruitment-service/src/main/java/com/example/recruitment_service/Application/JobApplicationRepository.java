package com.example.recruitment_service.Application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    boolean existsByCandidateIdAndOffer_Id(String candidateId, UUID offerId);

    @Override
    @EntityGraph(attributePaths = "offer")
    Optional<JobApplication> findById(UUID id);

    @EntityGraph(attributePaths = "offer")
    List<JobApplication> findByCandidateIdOrderBySubmittedAtDesc(String candidateId);

    @EntityGraph(attributePaths = "offer")
    Page<JobApplication> findByOffer_Id(UUID offerId, Pageable pageable);

    @EntityGraph(attributePaths = "offer")
    Page<JobApplication> findByOffer_IdAndStatus(UUID offerId, ApplicationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = "offer")
    Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "offer")
    Page<JobApplication> findAll(Pageable pageable);

    long countByOffer_Id(UUID offerId);
}
