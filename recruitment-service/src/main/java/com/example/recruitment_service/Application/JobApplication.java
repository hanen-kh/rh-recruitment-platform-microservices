package com.example.recruitment_service.Application;


import com.example.recruitment_service.JobOffer.JobOffer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "job_applications",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_application_candidate_offer",
                columnNames = {"candidate_id", "offer_id"}),
        indexes = {
                @Index(name = "idx_application_candidate", columnList = "candidate_id"),
                @Index(name = "idx_application_offer", columnList = "offer_id"),
                @Index(name = "idx_application_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /** Id du profil candidat = claim "sub" du JWT Keycloak (pas de FK : autre service, autre base). */
    @Column(name = "candidate_id", nullable = false, updatable = false, length = 64)
    private String candidateId;

    /** Id de l'offre. Adapte le type (Long/String/UUID) à ton service d'offres. */
    @JoinColumn(name = "offer_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private JobOffer offer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(name = "cover_letter", length = 4000)
    private String coverLetter;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** "sub" du HR_MANAGER/ADMIN qui a traité la candidature. */
    @Column(name = "reviewed_by", length = 64)
    private String reviewedBy;

    @Column(name = "review_note", length = 1000)
    private String reviewNote;

    /** Verrou optimiste : évite que deux RH écrasent mutuellement leur décision. */
    @Version
    private Long version;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.submittedAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /** Point d'entrée unique pour changer le statut : applique la règle des transitions. */
    public void changeStatus(ApplicationStatus next, String reviewer, String note) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException(
                    "Transition interdite : " + status + " -> " + next);
        }
        this.status = next;
        this.reviewedBy = reviewer;
        this.reviewNote = note;
    }
}