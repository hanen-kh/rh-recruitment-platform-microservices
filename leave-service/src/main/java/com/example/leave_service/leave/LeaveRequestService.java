package com.example.leave_service.leave;

import com.example.grpc.EmployeeGrpcServiceGrpc;
import com.example.grpc.EmployeeLookupRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeGrpcServiceGrpc.EmployeeGrpcServiceBlockingStub employeeStub;

    @Transactional
    public LeaveRequestResponse create(String employeeId, LeaveRequestCreateRequest request) {
        requireText(employeeId, "L'identifiant Keycloak de l'employé est obligatoire.");
        verifyEmployeeExists(employeeId);
        validateDateRange(request.startDate(), request.endDate());

        int requestedDays = countWeekdays(request.startDate(), request.endDate());
        if (requestedDays == 0) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "La période demandée ne contient aucun jour ouvrable.");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employeeId(employeeId)
                .leaveType(request.leaveType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .requestedDays(BigDecimal.valueOf(requestedDays).setScale(2))
                .reason(trimToNull(request.reason()))
                .status(LeaveStatus.PENDING)
                .build();

        return LeaveRequestResponse.fromEntity(leaveRequestRepository.save(leaveRequest));
    }

    private void verifyEmployeeExists(String employeeId) {
        try {
            employeeStub.withDeadlineAfter(3, TimeUnit.SECONDS)
                    .getEmployeeForLeave(EmployeeLookupRequest.newBuilder()
                            .setEmployeeId(employeeId)
                            .build());
        } catch (StatusRuntimeException exception) {
            Status.Code code = exception.getStatus().getCode();
            if (code == Status.Code.NOT_FOUND) {
                throw new ResponseStatusException(NOT_FOUND,
                        "Aucun employé ne correspond à l'identifiant fourni.", exception);
            }
            if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED) {
                throw new ResponseStatusException(SERVICE_UNAVAILABLE,
                        "Le service des employés est temporairement indisponible.", exception);
            }
            throw new ResponseStatusException(BAD_GATEWAY,
                    "La vérification de l'employé a échoué.", exception);
        }
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findMine(String employeeId) {
        requireText(employeeId, "L'identifiant Keycloak de l'employé est obligatoire.");
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(LeaveRequestResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeaveRequestResponse findMineById(Long id, String employeeId) {
        return LeaveRequestResponse.fromEntity(getOwnedRequest(id, employeeId));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> findPending() {
        return leaveRequestRepository.findByStatusOrderByCreatedAtAsc(LeaveStatus.PENDING).stream()
                .map(LeaveRequestResponse::fromEntity)
                .toList();
    }

    @Transactional
    public LeaveRequestResponse cancelMine(Long id, String employeeId) {
        LeaveRequest request = getOwnedRequest(id, employeeId);
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Seule une demande en attente peut être annulée.");
        }
        request.setStatus(LeaveStatus.CANCELLED);
        return LeaveRequestResponse.fromEntity(leaveRequestRepository.save(request));
    }

    @Transactional
    public LeaveRequestResponse decide(Long id, String reviewerId, LeaveDecisionRequest decision) {
        requireText(reviewerId, "L'identifiant Keycloak du valideur est obligatoire.");
        if (decision.status() != LeaveStatus.APPROVED && decision.status() != LeaveStatus.REJECTED) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Une décision doit être APPROVED ou REJECTED.");
        }

        LeaveRequest request = getRequest(id);
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Seule une demande en attente peut être traitée.");
        }
        if (request.getEmployeeId().equals(reviewerId)) {
            throw new ResponseStatusException(FORBIDDEN,
                    "Un employé ne peut pas valider sa propre demande.");
        }

        request.setStatus(decision.status());
        request.setReviewedById(reviewerId);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewComment(trimToNull(decision.reviewComment()));
        return LeaveRequestResponse.fromEntity(leaveRequestRepository.save(request));
    }

    private LeaveRequest getOwnedRequest(Long id, String employeeId) {
        LeaveRequest request = getRequest(id);
        if (!request.getEmployeeId().equals(employeeId)) {
            throw new ResponseStatusException(NOT_FOUND, "Demande de congé introuvable.");
        }
        return request;
    }

    private LeaveRequest getRequest(Long id) {
        if (id == null) {
            throw new ResponseStatusException(NOT_FOUND, "Identifiant de demande invalide.");
        }
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND,
                        "Demande de congé introuvable : " + id));
    }

    private static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "La date de fin doit être égale ou postérieure à la date de début.");
        }
    }

    private static int countWeekdays(LocalDate startDate, LocalDate endDate) {
        int weekdays = 0;
        LocalDate current = startDate;
        while (true) {
            DayOfWeek day = current.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                weekdays++;
            }
            if (current.equals(endDate)) {
                return weekdays;
            }
            current = current.plusDays(1);
        }
    }

    private static void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(BAD_REQUEST, message);
        }
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
