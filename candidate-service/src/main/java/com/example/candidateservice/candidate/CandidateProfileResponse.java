package com.example.candidateservice.candidate;

import java.time.LocalDateTime;

public record CandidateProfileResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String currentPosition,
        String cvFilePath,
        boolean lookingForJob,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CandidateProfileResponse fromEntity(CandidateProfile candidateProfile) {
        return new CandidateProfileResponse(
                candidateProfile.getId(),
                candidateProfile.getFirstName(),
                candidateProfile.getLastName(),
                candidateProfile.getEmail(),
                candidateProfile.getCurrentPosition(),
                candidateProfile.getCvFilePath(),
                candidateProfile.isLookingForJob(),
                candidateProfile.getCreatedAt(),
                candidateProfile.getUpdatedAt()
        );
    }
}
