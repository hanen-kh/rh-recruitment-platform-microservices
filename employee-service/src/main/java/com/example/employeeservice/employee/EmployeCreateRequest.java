package com.example.employeeservice.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeCreateRequest(
        @NotBlank @Size(max = 100)
        String id,
        @NotBlank @Size(max = 100)
        String firstName,
        @NotBlank @Size(max = 100)
        String lastName,
        @NotBlank @Email @Size(max = 254)
        String email,
        @Size(max = 30)
        String phoneNumber,
        @Size(max = 150)
        String jobTitle,
        @NotNull
        Department department,
        @NotNull
        EmployeeRole role,
        EmploymentStatus employmentStatus,
        LocalDate hireDate
) {
}
