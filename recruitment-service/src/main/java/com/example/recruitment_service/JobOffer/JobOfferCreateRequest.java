package com.example.recruitment_service.JobOffer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JobOfferCreateRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank String description,
        String requirements,
        @NotBlank @Size(max = 100) String department,
        @NotBlank @Size(max = 150) String location,
        @NotNull ContractType contractType,
        @NotNull WorkMode workMode,
        @DecimalMin("0.0") BigDecimal salaryMin,
        @DecimalMin("0.0") BigDecimal salaryMax,
        @Size(min = 3, max = 3) String salaryCurrency,
        @NotNull @Positive Integer openings,
        LocalDate applicationDeadline
) {}
