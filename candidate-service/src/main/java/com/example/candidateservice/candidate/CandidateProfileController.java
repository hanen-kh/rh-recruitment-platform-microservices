package com.example.candidateservice.candidate;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/api/candidates")
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    public CandidateProfileController(CandidateProfileService candidateProfileService) {
        this.candidateProfileService = candidateProfileService;
    }

    @PostMapping
    public ResponseEntity<CandidateProfileResponse> create(
            @Valid @RequestBody CandidateProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(candidateProfileService.create( request));
    }

    @GetMapping
    public List<CandidateProfileResponse> findAll(
            @RequestParam(required = false) Boolean lookingForJob
    ) {
        return candidateProfileService.findAll(lookingForJob);
    }

    @GetMapping("/{id}")
    public CandidateProfileResponse findById(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        requireOwnerOrStaff(id, jwt);
        return candidateProfileService.findById(id);
    }

    @PutMapping("/{id}")
    public CandidateProfileResponse update(
            @PathVariable String id,
            @RequestBody CandidateProfileRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        requireOwnerOrStaff(id, jwt);
        return candidateProfileService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        requireOwnerOrStaff(id, jwt);
        candidateProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CandidateProfileResponse uploadCv(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        requireOwnerOrStaff(id, jwt);
        return candidateProfileService.uploadCv(id, file);
    }

    private void requireOwnerOrStaff(String candidateId, Jwt jwt) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean staff = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_HR_MANAGER")
                        || authority.getAuthority().equals("ROLE_ADMIN"));
        if (!staff && (jwt == null || !candidateId.equals(jwt.getSubject()))) {
            throw new ResponseStatusException(FORBIDDEN, "Vous ne pouvez accéder qu'à votre propre profil.");
        }
    }
}
