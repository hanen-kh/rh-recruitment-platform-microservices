package com.example.recruitment_service.JobOffer;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-offers")
public class JobOfferController {

    private final JobOfferService jobOfferService;

    public JobOfferController(JobOfferService jobOfferService) {
        this.jobOfferService = jobOfferService;
    }

    @PostMapping
    public ResponseEntity<JobOfferResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody JobOfferCreateRequest request
    ) {
        JobOfferResponse created = jobOfferService.create(jwt.getSubject(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<JobOfferResponse> findPublished(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String title
    ) {
        return jobOfferService.findPublished(department, title);
    }

    @GetMapping("/mine")
    public List<JobOfferResponse> findMine(@AuthenticationPrincipal Jwt jwt) {
        return jobOfferService.findMine(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public JobOfferResponse findPublishedById(@PathVariable UUID id) {
        return jobOfferService.findPublishedById(id);
    }

    @PutMapping("/{id}")
    public JobOfferResponse updateDraft(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody JobOfferCreateRequest request
    ) {
        return jobOfferService.updateDraft(id, jwt.getSubject(), request);
    }

    @PostMapping("/{id}/publish")
    public JobOfferResponse publish(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return jobOfferService.publish(id, jwt.getSubject());
    }

    @PostMapping("/{id}/close")
    public JobOfferResponse close(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return jobOfferService.close(id, jwt.getSubject());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDraft(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        jobOfferService.deleteDraft(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
