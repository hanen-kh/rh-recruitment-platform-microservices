package com.example.candidateservice.cvanalysis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candidates/{candidateId}/cv-analysis")
public class CvAnalysisController {

    private final CvAnalysisService cvAnalysisService;

    public CvAnalysisController(CvAnalysisService cvAnalysisService) {
        this.cvAnalysisService = cvAnalysisService;
    }

    @PostMapping
    public CvAnalysisHistoryResponse analyzeCandidateCv(@PathVariable String candidateId) {
        return cvAnalysisService.analyzeCandidateCv(candidateId);
    }

    @GetMapping
    public List<CvAnalysisHistoryResponse> getCandidateAnalysisHistory(@PathVariable String candidateId) {
        return cvAnalysisService.getCandidateAnalysisHistory(candidateId);
    }
}
