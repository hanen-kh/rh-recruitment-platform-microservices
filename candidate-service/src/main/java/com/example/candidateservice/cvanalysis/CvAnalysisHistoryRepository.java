package com.example.candidateservice.cvanalysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CvAnalysisHistoryRepository extends JpaRepository<CvAnalysisHistory, Long> {

    List<CvAnalysisHistory> findByCandidateProfileIdOrderByAnalyzedAtDesc(String candidateId);

}
