package com.example.candidateservice.candidate;

public record CandidateProfileRequest(
        String firstName,
        String lastName,
        String email,
        String currentPosition,
        Boolean lookingForJob
) {
}
