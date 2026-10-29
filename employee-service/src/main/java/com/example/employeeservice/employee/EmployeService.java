package com.example.employeeservice.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class EmployeService {

    private final EmployeRepository employeRepository;

    @Transactional
    public EmployeResponse create(EmployeCreateRequest request) {
        if (employeRepository.existsById(request.id().trim())) {
            throw new ResponseStatusException(CONFLICT, "Un employé existe déjà avec cet identifiant Keycloak.");
        }
        ensureEmailIsAvailable(request.email(), null);

        Employe employe = Employe.builder()
                .id(request.id().trim())
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(normalizeEmail(request.email()))
                .phoneNumber(trimToNull(request.phoneNumber()))
                .jobTitle(trimToNull(request.jobTitle()))
                .department(request.department())
                .role(request.role())
                .employmentStatus(request.employmentStatus() == null
                        ? EmploymentStatus.ACTIVE
                        : request.employmentStatus())
                .hireDate(request.hireDate())
                .build();

        return EmployeResponse.fromEntity(employeRepository.save(employe));
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
}
