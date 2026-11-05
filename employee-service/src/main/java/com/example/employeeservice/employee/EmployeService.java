package com.example.employeeservice.employee;

import com.example.employeeservice.KeycloakUser.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

import static com.example.employeeservice.employee.EmployeeRole.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final KeycloakUserService keycloakUserService;

    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public EmployeResponse create(EmployeCreateRequest request, Authentication caller) {
        String email = normalizeEmail(request.email());
        ensureEmailIsAvailable(email, null);
        ensureCallerCanAssign(caller, request.role());

        // 1. Création dans Keycloak : l'id retourné = claim "sub" du futur JWT
        String keycloakId = keycloakUserService.createUser(
                email,
                request.firstName().trim(),
                request.lastName().trim(),
                request.role().name(),
                generateTempPassword());

        try {
            Employe employe = Employe.builder()
                    .id(keycloakId)
                    .firstName(request.firstName().trim())
                    .lastName(request.lastName().trim())
                    .email(email)
                    .phoneNumber(trimToNull(request.phoneNumber()))
                    .jobTitle(trimToNull(request.jobTitle()))
                    .department(request.department())
                    .role(request.role())
                    .employmentStatus(request.employmentStatus() == null
                            ? EmploymentStatus.ACTIVE
                            : request.employmentStatus())
                    .hireDate(request.hireDate())
                    .build();

            // saveAndFlush : force l'INSERT maintenant, pour que l'erreur éventuelle
            // soit levée ici et non au commit (après la fin de la méthode)
            return EmployeResponse.fromEntity(employeRepository.saveAndFlush(employe));
        } catch (RuntimeException ex) {
            // 2. Compensation : @Transactional ne sait pas annuler Keycloak
            keycloakUserService.deleteUser(keycloakId);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<EmployeResponse> findAll() {
        return employeRepository.findAll().stream()
                .map(EmployeResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeResponse findById(String id) {
        return EmployeResponse.fromEntity(getEmploye(id));
    }

    @Transactional
    public EmployeResponse update(String id, EmployeUpdateRequest request) {
        Employe employe = getEmploye(id);
        ensureEmailIsAvailable(request.email(), employe.getId());

        employe.setFirstName(request.firstName().trim());
        employe.setLastName(request.lastName().trim());
        employe.setEmail(normalizeEmail(request.email()));
        employe.setPhoneNumber(trimToNull(request.phoneNumber()));
        employe.setJobTitle(trimToNull(request.jobTitle()));
        employe.setDepartment(request.department());
        employe.setRole(request.role());
        employe.setEmploymentStatus(request.employmentStatus());
        employe.setHireDate(request.hireDate());

        return EmployeResponse.fromEntity(employeRepository.save(employe));
    }

    @Transactional
    public void delete(String id) {
        employeRepository.delete(getEmploye(id));
    }

    private Employe getEmploye(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Identifiant de l'employé introuvable.");
        }
        return employeRepository.findById(id.trim())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employé introuvable : " + id));
    }

    private void ensureEmailIsAvailable(String email, String currentId) {
        String normalizedEmail = normalizeEmail(email);
        boolean alreadyUsed = currentId == null
                ? employeRepository.existsByEmailIgnoreCase(normalizedEmail)
                : employeRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, currentId);
        if (alreadyUsed) {
            throw new ResponseStatusException(CONFLICT, "Cette adresse email est déjà utilisée.");
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void ensureCallerCanAssign(Authentication caller, EmployeeRole requested) {
        boolean callerIsAdmin = hasRole(caller, "ROLE_ADMIN");
        boolean callerIsHr    = hasRole(caller, "ROLE_HR_MANAGER");

        boolean allowed = switch (requested) {
            case ADMIN -> callerIsAdmin;
            case HR_MANAGER, EMPLOYEE -> callerIsAdmin || callerIsHr;
        };

        if (!allowed) {
            throw new ResponseStatusException(FORBIDDEN,
                    "Vous n'avez pas le droit de créer un employé avec le rôle " + requested);
        }
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
    private String generateTempPassword() {
        StringBuilder sb = new StringBuilder(14);
        for (int i = 0; i < 14; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
