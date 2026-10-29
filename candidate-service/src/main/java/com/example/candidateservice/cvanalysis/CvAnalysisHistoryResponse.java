package com.example.candidateservice.cvanalysis;

import java.time.LocalDateTime;

public record CvAnalysisHistoryResponse(
        Long id,
        String candidateProfileId,
        String rawAnalysisReport,
        String generatedInterviewQuestions,
        String modelUsed,
        LocalDateTime analyzedAt
) {

    public static CvAnalysisHistoryResponse fromEntity(CvAnalysisHistory history) {
        return new CvAnalysisHistoryResponse(
                history.getId(),
                history.getCandidateProfile().getId(),
                history.getRawAnalysisReport(),
                history.getGeneratedInterviewQuestions(),
                history.getModelUsed(),
                history.getAnalyzedAt()
        );
    }
}
