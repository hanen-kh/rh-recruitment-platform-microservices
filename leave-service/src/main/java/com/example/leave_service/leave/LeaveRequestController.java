package com.example.leave_service.leave;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    public ResponseEntity<LeaveRequestResponse> create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LeaveRequestCreateRequest request
    ) {
        LeaveRequestResponse created = leaveRequestService.create(jwt.getSubject(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/mine")
    public List<LeaveRequestResponse> findMine(@AuthenticationPrincipal Jwt jwt) {
        return leaveRequestService.findMine(jwt.getSubject());
    }

    @GetMapping("/pending")
    public List<LeaveRequestResponse> findPending() {
        return leaveRequestService.findPending();
    }

    @GetMapping("/{id}")
    public LeaveRequestResponse findMineById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return leaveRequestService.findMineById(id, jwt.getSubject());
    }

    @DeleteMapping("/{id}")
    public LeaveRequestResponse cancelMine(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return leaveRequestService.cancelMine(id, jwt.getSubject());
    }

    @PutMapping("/{id}/decision")
    public LeaveRequestResponse decide(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LeaveDecisionRequest decision
    ) {
        return leaveRequestService.decide(id, jwt.getSubject(), decision);
    }
}
