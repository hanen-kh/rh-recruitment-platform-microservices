package com.example.recruitment_service.Application;


import com.example.recruitment_service.JobOffer.JobOffer;
import com.example.recruitment_service.JobOffer.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobApplicationService {

    private static final ZoneId ZONE = ZoneId.of("Africa/Tunis");

    private final JobApplicationRepository repository;
    private final JobOfferRepository jobOfferRepository;

    // ─── Candidat ──────────────────────────────────────────────

    @Transactional
    public ApplicationResponse apply(String candidateId, JobApplicationRequest request) {
        JobOffer offer = jobOfferRepository.findById(request.offerId())
                .orElseThrow(() -> new ResponseStatusException(UNPROCESSABLE_ENTITY,
                        "Offre introuvable : " + request.offerId()));

        LocalDate today = today();
        if (!offer.isOpenForApplications(today)) {
            throw new ResponseStatusException(CONFLICT,
                    "Cette offre n'accepte plus de candidatures.");
        }
        if (repository.existsByCandidateIdAndOffer_Id(candidateId, offer.getId())) {
            throw alreadyApplied(offer.getId());
        }

        JobApplication application = JobApplication.builder()
                .candidateId(candidateId)
                .offer(offer)
                .coverLetter(trimToNull(request.coverLetter()))
                .build();
        try {
            JobApplication saved = repository.saveAndFlush(application);
            log.info("Candidature {} créée (offre {})", saved.getId(), offer.getId());
            return ApplicationResponse.from(saved, today);
        } catch (DataIntegrityViolationException e) {
            // Deux requêtes simultanées : la contrainte unique en base fait foi
            throw alreadyApplied(offer.getId());
        }
    }

    public List<ApplicationResponse> findMine(String candidateId) {
        LocalDate today = today();
        return repository.findByCandidateIdOrderBySubmittedAtDesc(candidateId).stream()
                .map(application -> ApplicationResponse.from(application, today))
                .toList();
    }

    public ApplicationResponse findById(UUID id, Caller caller) {
        return ApplicationResponse.from(loadVisible(id, caller), today());
    }

    @Transactional
    public ApplicationResponse withdraw(UUID id, Caller caller) {
        JobApplication application = loadVisible(id, caller);
        if (!caller.isOwnerOf(application)) {
            throw notFound(id);   // un RH ne retire pas la candidature d'un autre
        }
        transition(application, ApplicationStatus.WITHDRAWN, null, null);
        log.info("Candidature {} retirée par le candidat", id);
        return ApplicationResponse.from(application, today());
    }

    // ─── RH (HR_MANAGER / ADMIN) ───────────────────────────────

    public Page<ApplicationResponse> search(UUID offerId, ApplicationStatus status,
                                            Pageable pageable, Caller caller) {
        requireStaff(caller);

        Page<JobApplication> page;
        if (offerId != null && status != null) {
            page = repository.findByOffer_IdAndStatus(offerId, status, pageable);
        } else if (offerId != null) {
            page = repository.findByOffer_Id(offerId, pageable);
        } else if (status != null) {
            page = repository.findByStatus(status, pageable);
        } else {
            page = repository.findAll(pageable);
        }
        LocalDate today = today();
        return page.map(application -> ApplicationResponse.from(application, today));
    }

    public long countByOffer(UUID offerId, Caller caller) {
        requireStaff(caller);
        return repository.countByOffer_Id(offerId);
    }

    @Transactional
    public ApplicationResponse review(UUID id, JobApplicationStatusRequest request, Caller caller) {
        requireStaff(caller);
        if (request.status() == ApplicationStatus.WITHDRAWN) {
            throw new ResponseStatusException(FORBIDDEN,
                    "Seul le candidat peut retirer sa candidature.");
        }
        JobApplication application = loadVisible(id, caller);
        transition(application, request.status(), caller.subject(), trimToNull(request.note()));
        log.info("Candidature {} : statut {} par {}", id, request.status(), caller.subject());
        return ApplicationResponse.from(application, today());   // dirty checking : pas de save()
    }

    // ─── Méthodes internes ─────────────────────────────────────

    /** 404 (et non 403) pour un tiers : on ne révèle pas l'existence de la candidature. */
    private JobApplication loadVisible(UUID id, Caller caller) {
        JobApplication application = repository.findById(id)
                .orElseThrow(() -> notFound(id));
        if (!caller.staff() && !caller.isOwnerOf(application)) {
            throw notFound(id);
        }
        return application;
    }

    private void transition(JobApplication application, ApplicationStatus next,
                            String reviewer, String note) {
        if (!application.getStatus().canTransitionTo(next)) {
            throw new ResponseStatusException(CONFLICT,
                    "Transition interdite : " + application.getStatus() + " -> " + next);
        }
        application.changeStatus(next, reviewer, note);
    }

    private void requireStaff(Caller caller) {
        if (!caller.staff()) {
            throw new ResponseStatusException(FORBIDDEN, "Action réservée au personnel RH.");
        }
    }

    private static LocalDate today() {
        return LocalDate.now(ZONE);
    }

    private static ResponseStatusException notFound(UUID id) {
        return new ResponseStatusException(NOT_FOUND, "Candidature introuvable : " + id);
    }

    private static ResponseStatusException alreadyApplied(UUID offerId) {
        return new ResponseStatusException(CONFLICT,
                "Vous avez déjà postulé à l'offre " + offerId);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
