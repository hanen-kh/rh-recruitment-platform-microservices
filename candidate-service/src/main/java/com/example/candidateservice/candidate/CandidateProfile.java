package com.example.candidateservice.candidate;

import com.example.candidateservice.cvanalysis.CvAnalysisHistory;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee_cvs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfile {

    @Id
    private String id; // ID unique partagé avec Keycloak (le champ 'sub' du jeton JWT)

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String currentPosition;

    private String cvFilePath; // Chemin de stockage du fichier PDF/Docx (ex: stockage local ou MinIO)

    private boolean isLookingForJob; // Statut de recherche (Utile pour filtrer les candidats)

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Relation avec l'historique des analyses générées par l'IA
    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CvAnalysisHistory> analysisHistory = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
