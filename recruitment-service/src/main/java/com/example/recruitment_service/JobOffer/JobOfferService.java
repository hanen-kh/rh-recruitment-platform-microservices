package com.example.recruitment_service.JobOffer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobOfferService {

    private final JobOfferRepository jobOfferRepository;

    @Transactional
    public JobOfferResponse create(String creatorId, JobOfferCreateRequest request) {
        requireText(creatorId, "L'identifiant Keycloak du créateur est obligatoire.");
        validateRequest(request);
        JobOffer offer = JobOffer.builder()
                .title(request.title().trim())
                .description(request.description().trim())
                .requirements(trimToNull(request.requirements()))
                .department(request.department().trim())
                .location(request.location().trim())
                .contractType(request.contractType())
                .workMode(request.workMode())
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .salaryCurrency(normalizeCurrency(request.salaryCurrency()))
                .openings(request.openings())
                .applicationDeadline(request.applicationDeadline())
                .status(JobOfferStatus.DRAFT)
                .createdById(creatorId)
                .build();
        return JobOfferResponse.fromEntity(jobOfferRepository.save(offer));
    }

    @Transactional(readOnly = true)
    public List<JobOfferResponse> findPublished(String department, String title) {
        String normalizedDepartment = trimToNull(department);
        String normalizedTitle = trimToNull(title);
        List<JobOffer> offers;
        if (normalizedDepartment != null) {
            offers = jobOfferRepository.findByStatusAndDepartmentIgnoreCaseOrderByPublishedAtDesc(
                    JobOfferStatus.PUBLISHED, normalizedDepartment);
        } else if (normalizedTitle != null) {
            offers = jobOfferRepository.findByStatusAndTitleContainingIgnoreCaseOrderByPublishedAtDesc(
                    JobOfferStatus.PUBLISHED, normalizedTitle);
        } else {
            offers = jobOfferRepository.findByStatusOrderByPublishedAtDesc(JobOfferStatus.PUBLISHED);
        }
        return offers.stream().map(JobOfferResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public JobOfferResponse findPublishedById(UUID id) {
        JobOffer offer = getOffer(id);
        if (offer.getStatus() != JobOfferStatus.PUBLISHED) {
            throw notFound(id);
        }
        return JobOfferResponse.fromEntity(offer);
    }

    @Transactional(readOnly = true)
    public List<JobOfferResponse> findMine(String creatorId) {
        requireText(creatorId, "L'identifiant Keycloak du créateur est obligatoire.");
        return jobOfferRepository.findByCreatedByIdOrderByCreatedAtDesc(creatorId).stream()
                .map(JobOfferResponse::fromEntity).toList();
    }

    @Transactional
    public JobOfferResponse updateDraft(UUID id, String creatorId, JobOfferCreateRequest request) {
        JobOffer offer = getOwnedOffer(id, creatorId);
        if (offer.getStatus() != JobOfferStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une offre en brouillon peut être modifiée.");
        }
        validateRequest(request);
        offer.setTitle(request.title().trim());
        offer.setDescription(request.description().trim());
        offer.setRequirements(trimToNull(request.requirements()));
        offer.setDepartment(request.department().trim());
        offer.setLocation(request.location().trim());
        offer.setContractType(request.contractType());
        offer.setWorkMode(request.workMode());
        offer.setSalaryMin(request.salaryMin());
        offer.setSalaryMax(request.salaryMax());
        offer.setSalaryCurrency(normalizeCurrency(request.salaryCurrency()));
        offer.setOpenings(request.openings());
        offer.setApplicationDeadline(request.applicationDeadline());
        return JobOfferResponse.fromEntity(jobOfferRepository.save(offer));
    }

    @Transactional
    public JobOfferResponse publish(UUID id, String creatorId) {
        JobOffer offer = getOwnedOffer(id, creatorId);
        if (offer.getStatus() != JobOfferStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une offre en brouillon peut être publiée.");
        }
        if (offer.getApplicationDeadline() != null && offer.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La date limite de candidature ne peut pas être passée.");
        }
        offer.setStatus(JobOfferStatus.PUBLISHED);
        offer.setPublishedAt(LocalDateTime.now());
        return JobOfferResponse.fromEntity(jobOfferRepository.save(offer));
    }

    @Transactional
    public JobOfferResponse close(UUID id, String creatorId) {
        JobOffer offer = getOwnedOffer(id, creatorId);
        if (offer.getStatus() != JobOfferStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une offre publiée peut être clôturée.");
        }
        offer.setStatus(JobOfferStatus.CLOSED);
        return JobOfferResponse.fromEntity(jobOfferRepository.save(offer));
    }

    @Transactional
    public void deleteDraft(UUID id, String creatorId) {
        JobOffer offer = getOwnedOffer(id, creatorId);
        if (offer.getStatus() != JobOfferStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une offre en brouillon peut être supprimée.");
        }
        jobOfferRepository.delete(offer);
    }

    private JobOffer getOwnedOffer(UUID id, String creatorId) {
        requireText(creatorId, "L'identifiant Keycloak du créateur est obligatoire.");
        JobOffer offer = getOffer(id);
        if (!offer.getCreatedById().equals(creatorId)) {
            throw notFound(id);
        }
        return offer;
    }

    private JobOffer getOffer(UUID id) {
        if (id == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable.");
        return jobOfferRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private static void validateRequest(JobOfferCreateRequest request) {
        if (request.salaryMin() != null && request.salaryMax() != null
                && request.salaryMin().compareTo(request.salaryMax()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le salaire minimum ne peut pas dépasser le salaire maximum.");
        }
        if ((request.salaryMin() != null || request.salaryMax() != null)
                && !StringUtils.hasText(request.salaryCurrency())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La devise est obligatoire lorsqu'un salaire est renseigné.");
        }
    }

    private static ResponseStatusException notFound(UUID id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable : " + id);
    }

    private static void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static String normalizeCurrency(String currency) {
        String value = trimToNull(currency);
        return value == null ? null : value.toUpperCase();
    }
}
