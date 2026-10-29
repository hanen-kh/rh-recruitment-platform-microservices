package com.example.candidateservice.cvanalysis;

import com.example.candidateservice.candidate.CandidateProfile;
import com.example.candidateservice.candidate.CandidateProfileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class CvAnalysisService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final CvAnalysisHistoryRepository analysisHistoryRepository;
    private final FileStorageService fileStorageService;
    private final CvAiService cvAiService;
    private final String modelUsed;

    public CvAnalysisService(
            CandidateProfileRepository candidateProfileRepository,
            CvAnalysisHistoryRepository analysisHistoryRepository,
            FileStorageService fileStorageService,
            CvAiService cvAiService,
            @Value("${spring.ai.ollama.chat.model:${spring.ai.ollama.chat.options.model:unknown}}") String modelUsed
    ) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.analysisHistoryRepository = analysisHistoryRepository;
        this.fileStorageService = fileStorageService;
        this.cvAiService = cvAiService;
        this.modelUsed = modelUsed;
    }

    @Transactional
    public CvAnalysisHistoryResponse analyzeCandidateCv(String candidateId) {
        CandidateProfile candidateProfile = candidateProfileRepository.findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profil candidat introuvable : " + candidateId));

        if (!StringUtils.hasText(candidateProfile.getCvFilePath())) {
            throw new ResponseStatusException(BAD_REQUEST, "Aucun CV n'est associe a ce candidat.");
        }

        String cvText = extractCvText(candidateProfile.getCvFilePath());
        if (!StringUtils.hasText(cvText)) {
            throw new ResponseStatusException(BAD_REQUEST, "Le fichier CV ne contient pas de texte exploitable.");
        }

        String analysisReport = cvAiService.generateAnalysisReport(cvText);
        String interviewQuestions = cvAiService.generateInterviewQuestions(cvText);

        CvAnalysisHistory history = CvAnalysisHistory.builder()
                .candidateProfile(candidateProfile)
                .rawAnalysisReport(analysisReport)
                .generatedInterviewQuestions(interviewQuestions)
                .modelUsed(modelUsed)
                .build();

        return CvAnalysisHistoryResponse.fromEntity(analysisHistoryRepository.save(history));
    }

    @Transactional(readOnly = true)
    public List<CvAnalysisHistoryResponse> getCandidateAnalysisHistory(String candidateId) {
        if (!candidateProfileRepository.existsById(candidateId)) {
            throw new ResponseStatusException(NOT_FOUND, "Profil candidat introuvable : " + candidateId);
        }

        return analysisHistoryRepository.findByCandidateProfileIdOrderByAnalyzedAtDesc(candidateId)
                .stream()
                .map(CvAnalysisHistoryResponse::fromEntity)
                .toList();
    }

    private String extractCvText(String cvFilePath) {
        try {
            return fileStorageService.extractText(cvFilePath);
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, e.getMessage(), e);
        }
    }
}
