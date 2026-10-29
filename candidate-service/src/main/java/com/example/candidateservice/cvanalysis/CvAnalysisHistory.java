package com.example.candidateservice.cvanalysis;

import com.example.candidateservice.candidate.CandidateProfile;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cv_analysis_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CvAnalysisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Utilisation d'un gros bloc de texte pour stocker le rapport Markdown d'Ollama
    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String rawAnalysisReport;

    // Stockage spécifique des 5 questions d'entretien personnalisées générées par l'IA
    @Lob
    @Column(columnDefinition = "TEXT")
    private String generatedInterviewQuestions;

    @Column(nullable = false)
    private String modelUsed; // Exemple: "llama3", "mistral" (pour l'observabilité)

    private LocalDateTime analyzedAt;

    // Liaison avec le profil candidat
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_cv_id", nullable = false)
    private CandidateProfile candidateProfile;

    @PrePersist
    protected void onAnalyze() {
        this.analyzedAt = LocalDateTime.now();
    }
}
