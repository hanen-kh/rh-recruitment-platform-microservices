package com.example.employeeservice.employee;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EmployeResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String jobTitle,
        Department department,
        EmployeeRole role,
        EmploymentStatus employmentStatus,
        LocalDate hireDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EmployeResponse fromEntity(Employe employe) {
        return new EmployeResponse(
                employe.getId(),
                employe.getFirstName(),
                employe.getLastName(),
                employe.getEmail(),
                employe.getPhoneNumber(),
                employe.getJobTitle(),
                employe.getDepartment(),
                employe.getRole(),
                employe.getEmploymentStatus(),
                employe.getHireDate(),
                employe.getCreatedAt(),
                employe.getUpdatedAt()
        );
    }
}
